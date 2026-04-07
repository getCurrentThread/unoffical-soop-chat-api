package com.github.getcurrentthread.soopapi.websocket;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import com.github.getcurrentthread.soopapi.config.SOOPChatConfig;
import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.EventEmitter;
import com.github.getcurrentthread.soopapi.event.model.ReconnectedEvent;
import com.github.getcurrentthread.soopapi.event.model.ReconnectingEvent;
import com.github.getcurrentthread.soopapi.exception.ConnectionException;
import com.github.getcurrentthread.soopapi.model.ChannelInfo;
import com.github.getcurrentthread.soopapi.util.SSLContextProvider;

public class WebSocketManager implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(WebSocketManager.class.getName());
    private static final long INITIAL_RETRY_DELAY_MS = 2000;
    private static final long MAX_RETRY_DELAY_MS = 30000;

    private record WsState(boolean connected, WebSocket ws, ScheduledFuture<?> ping) {}

    private final SOOPChatConfig config;
    private final HttpClient httpClient;
    private final ScheduledExecutorService scheduler;
    private final WebSocketListener listener;
    private final EventEmitter eventEmitter;
    private final AtomicReference<WsState> wsState =
            new AtomicReference<>(new WsState(false, null, null));
    private final AtomicInteger retryCount;
    private final AtomicReference<ChannelInfo> currentChannelInfo = new AtomicReference<>();
    private final AtomicReference<CompletableFuture<Void>> reconnectFuture =
            new AtomicReference<>();

    public WebSocketManager(
            SOOPChatConfig config,
            SSLContext sslContext,
            ScheduledExecutorService scheduler,
            WebSocketListener listener,
            EventEmitter eventEmitter) {
        this.config = config;
        this.scheduler = scheduler;
        this.listener = listener;
        this.eventEmitter = Objects.requireNonNull(eventEmitter, "eventEmitter");
        this.retryCount = new AtomicInteger(0);

        SSLContext ssl = sslContext != null ? sslContext : SSLContextProvider.getInstance();
        this.httpClient =
                HttpClient.newBuilder()
                        .sslContext(ssl)
                        .connectTimeout(config.getConnectionTimeout())
                        .build();
        LOGGER.fine("WebSocketManager initialized");
    }

    /**
     * WebSocket에 연결합니다.
     *
     * @param channelInfo 연결할 채널 정보
     * @return 연결이 수립되면 완료되는 CompletableFuture
     */
    public CompletableFuture<Void> connect(ChannelInfo channelInfo) {
        currentChannelInfo.set(channelInfo);
        LOGGER.fine(
                () ->
                        "Attempting to connect to WebSocket: "
                                + channelInfo.CHDOMAIN()
                                + ":"
                                + channelInfo.CHPT());

        CompletableFuture<Void> connectionFuture = new CompletableFuture<>();

        try {
            URI uri = buildWebSocketUri(channelInfo);
            LOGGER.fine(() -> "Connecting to URI: " + uri);

            httpClient
                    .newWebSocketBuilder()
                    .subprotocols("chat")
                    .connectTimeout(config.getConnectionTimeout())
                    .buildAsync(uri, listener)
                    .thenAccept(
                            ws -> {
                                try {
                                    LOGGER.info("WebSocket connection established");
                                    handleConnectionSuccess(ws, channelInfo, connectionFuture);
                                } catch (Exception e) {
                                    LOGGER.log(Level.SEVERE, "Error during connection setup", e);
                                    connectionFuture.completeExceptionally(e);
                                }
                            })
                    .exceptionally(
                            throwable -> {
                                LOGGER.log(
                                        Level.SEVERE,
                                        "Failed to establish WebSocket connection",
                                        throwable);
                                scheduleRetry(channelInfo, throwable, connectionFuture);
                                return null;
                            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initiating WebSocket connection", e);
            connectionFuture.completeExceptionally(e);
        }

        return connectionFuture;
    }

    private CompletableFuture<Void> sendInitialPackets(ChannelInfo channelInfo) {
        String authTicket = null;
        String uuid = null;
        if (config.isAuthenticated()) {
            authTicket = config.getAuthCookie().authTicket();
            uuid = config.getAuthCookie().au();
        }

        LOGGER.fine("Sending connect packet...");
        String connectPacket = WebSocketPacketBuilder.createConnectPacket(authTicket);
        String joinPacket = WebSocketPacketBuilder.createJoinPacket(channelInfo, authTicket, uuid);

        WsState state = wsState.get();
        WebSocket ws = state.ws();
        if (ws == null) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("WebSocket is not connected"));
        }

        return ws.sendText(connectPacket, true)
                .thenCompose(
                        _ -> {
                            LOGGER.info("Connect packet sent successfully, sending join packet...");
                            WsState current = wsState.get();
                            WebSocket currentWs = current.ws();
                            if (currentWs == null) {
                                return CompletableFuture.failedFuture(
                                        new IllegalStateException("WebSocket disconnected"));
                            }
                            return currentWs.sendText(joinPacket, true);
                        })
                .thenRun(() -> LOGGER.info("Join packet sent successfully"));
    }

    /** 단일 재시도를 스케줄링합니다. 실패 시 다음 재시도를 스케줄링합니다 (connect()를 통한 재귀가 아닌 반복 방식). */
    private void scheduleRetry(
            ChannelInfo channelInfo,
            Throwable throwable,
            CompletableFuture<Void> connectionFuture) {
        LOGGER.log(Level.WARNING, "WebSocket connection error", throwable);

        int currentRetry = retryCount.incrementAndGet();

        if (currentRetry > config.getMaxRetryAttempts()) {
            LOGGER.severe("Max retry attempts reached. Connection failed permanently.");
            if (!connectionFuture.isDone()) {
                connectionFuture.completeExceptionally(throwable);
            }
            return;
        }

        long delay = calculateExponentialBackoff(currentRetry);

        LOGGER.fine(
                "Attempting retry "
                        + currentRetry
                        + " of "
                        + config.getMaxRetryAttempts()
                        + " in "
                        + delay
                        + "ms");

        eventEmitter.emit(
                ChatEvent.RECONNECTING,
                new ReconnectingEvent(
                        currentRetry,
                        config.getMaxRetryAttempts(),
                        delay,
                        ChatEvent.RECONNECTING,
                        "",
                        System.currentTimeMillis()));

        CompletableFuture<Void> newFuture = new CompletableFuture<>();
        reconnectFuture.set(newFuture);

        scheduler.schedule(
                () -> {
                    try {
                        doSingleConnectAttempt(channelInfo)
                                .thenRun(
                                        () -> {
                                            CompletableFuture<Void> rf = reconnectFuture.get();
                                            if (rf != null) {
                                                rf.complete(null);
                                            }
                                            eventEmitter.emit(
                                                    ChatEvent.RECONNECTED,
                                                    new ReconnectedEvent(
                                                            currentRetry,
                                                            ChatEvent.RECONNECTED,
                                                            "",
                                                            System.currentTimeMillis()));
                                            if (!connectionFuture.isDone()) {
                                                connectionFuture.complete(null);
                                            }
                                        })
                                .exceptionally(
                                        e -> {
                                            LOGGER.log(Level.SEVERE, "Retry attempt failed", e);
                                            scheduleRetry(channelInfo, e, connectionFuture);
                                            return null;
                                        });
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error during reconnection", e);
                        CompletableFuture<Void> rf = reconnectFuture.get();
                        if (rf != null) {
                            rf.completeExceptionally(e);
                        }
                    }
                },
                delay,
                TimeUnit.MILLISECONDS);
    }

    /** 재시도 로직 없이 단일 WebSocket 연결을 시도합니다. 재귀적 connect() 호출을 피하기 위해 scheduleRetry()에서 사용됩니다. */
    private CompletableFuture<Void> doSingleConnectAttempt(ChannelInfo channelInfo) {
        currentChannelInfo.set(channelInfo);
        CompletableFuture<Void> connectionFuture = new CompletableFuture<>();

        try {
            URI uri = buildWebSocketUri(channelInfo);

            httpClient
                    .newWebSocketBuilder()
                    .subprotocols("chat")
                    .connectTimeout(config.getConnectionTimeout())
                    .buildAsync(uri, listener)
                    .thenAccept(ws -> handleConnectionSuccess(ws, channelInfo, connectionFuture))
                    .exceptionally(
                            t -> {
                                connectionFuture.completeExceptionally(t);
                                return null;
                            });
        } catch (Exception e) {
            connectionFuture.completeExceptionally(e);
        }

        return connectionFuture;
    }

    private URI buildWebSocketUri(ChannelInfo channelInfo) throws java.net.URISyntaxException {
        int portNumber;
        try {
            portNumber = Integer.parseInt(channelInfo.CHPT());
        } catch (NumberFormatException e) {
            throw new ConnectionException("Invalid port number format: " + channelInfo.CHPT(), e);
        }
        return new URI(
                "wss",
                null,
                channelInfo.CHDOMAIN(),
                portNumber,
                "/Websocket/" + config.getBid(),
                null,
                null);
    }

    private void handleConnectionSuccess(
            WebSocket ws, ChannelInfo channelInfo, CompletableFuture<Void> connectionFuture) {
        wsState.set(new WsState(true, ws, null));
        retryCount.set(0);

        sendInitialPackets(channelInfo)
                .thenRun(
                        () -> {
                            startPingScheduler();
                            connectionFuture.complete(null);
                        })
                .exceptionally(
                        e -> {
                            LOGGER.log(Level.SEVERE, "Failed to send initial packets", e);
                            connectionFuture.completeExceptionally(e);
                            return null;
                        });
    }

    private long calculateExponentialBackoff(int retryCount) {
        long delay = INITIAL_RETRY_DELAY_MS * (long) Math.pow(2, retryCount - 1);
        return Math.min(delay, MAX_RETRY_DELAY_MS);
    }

    private void startPingScheduler() {
        WsState current = wsState.get();
        if (current.ping() != null && !current.ping().isDone()) {
            current.ping().cancel(false);
        }

        long pingInterval = config.getPingIntervalSeconds();
        String pingPacket = WebSocketPacketBuilder.createPingPacket();
        ScheduledFuture<?> newPingTask =
                scheduler.scheduleWithFixedDelay(
                        () -> {
                            WsState state = wsState.get();
                            if (state.connected() && state.ws() != null) {
                                try {
                                    LOGGER.fine("Sending ping packet");
                                    state.ws().sendText(pingPacket, true).join();
                                } catch (Exception e) {
                                    LOGGER.log(Level.WARNING, "Error sending ping", e);
                                    WsState s = wsState.get();
                                    if (s.connected()) {
                                        cleanupResources();
                                        LOGGER.fine("Ping failed, attempting to reconnect");
                                        emitReconnecting();
                                        reconnect()
                                                .thenRun(this::emitReconnected)
                                                .exceptionally(
                                                        ex -> {
                                                            LOGGER.log(
                                                                    Level.SEVERE,
                                                                    "Ping-triggered reconnect"
                                                                            + " failed",
                                                                    ex);
                                                            return null;
                                                        });
                                    }
                                }
                            }
                        },
                        pingInterval,
                        pingInterval,
                        TimeUnit.SECONDS);

        // 새로운 ping 작업으로 상태를 원자적으로 업데이트하며, 정리(cleanup) 경쟁 상태를 확인
        wsState.updateAndGet(
                s -> {
                    if (!s.connected()) {
                        newPingTask.cancel(false);
                        return s;
                    }
                    return new WsState(s.connected(), s.ws(), newPingTask);
                });
    }

    /**
     * 현재 채널 정보를 사용하여 연결을 재시도합니다.
     *
     * @return 재연결이 성공하면 완료되는 CompletableFuture
     */
    public CompletableFuture<Void> reconnect() {
        ChannelInfo channelInfo = currentChannelInfo.get();
        if (channelInfo == null) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException(
                            "No previous channel info available for reconnection"));
        }

        CompletableFuture<Void> existing = reconnectFuture.get();
        if (existing != null && !existing.isDone()) {
            return existing;
        }

        cleanupResources();
        return connect(channelInfo);
    }

    public void disconnect() {
        WsState state = wsState.get();
        if (state.ws() != null) {
            try {
                LOGGER.info("Initiating WebSocket disconnect");
                state.ws().sendClose(WebSocket.NORMAL_CLOSURE, "Disconnecting");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error during disconnect", e);
            } finally {
                cleanupResources();
            }
        }
    }

    private void cleanupResources() {
        LOGGER.fine("Cleaning up WebSocket resources");
        WsState old = wsState.getAndSet(new WsState(false, null, null));
        if (old.ping() != null) {
            old.ping().cancel(false);
        }
    }

    private void emitReconnecting() {
        int current = retryCount.get();
        eventEmitter.emit(
                ChatEvent.RECONNECTING,
                new ReconnectingEvent(
                        current,
                        config.getMaxRetryAttempts(),
                        calculateExponentialBackoff(current),
                        ChatEvent.RECONNECTING,
                        "",
                        System.currentTimeMillis()));
    }

    private void emitReconnected() {
        eventEmitter.emit(
                ChatEvent.RECONNECTED,
                new ReconnectedEvent(
                        retryCount.get(), ChatEvent.RECONNECTED, "", System.currentTimeMillis()));
    }

    /**
     * 현재 WebSocket 연결 상태를 반환합니다.
     *
     * @return 연결 상태
     */
    public CompletableFuture<WebSocketStatus> getStatus() {
        if (!isConnected()) {
            return CompletableFuture.completedFuture(
                    new WebSocketStatus(false, retryCount.get(), config.getMaxRetryAttempts()));
        }
        return CompletableFuture.completedFuture(
                new WebSocketStatus(true, 0, config.getMaxRetryAttempts()));
    }

    public record WebSocketStatus(boolean connected, int retryCount, int maxRetries) {}

    @Override
    public void close() {
        disconnect();
        httpClient.close();
    }

    public boolean isConnected() {
        WsState state = wsState.get();
        return state.connected() && state.ws() != null;
    }

    public CompletableFuture<Void> sendChat(String message) {
        WsState state = wsState.get();
        if (!state.connected() || state.ws() == null) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("WebSocket is not connected"));
        }
        String packet = WebSocketPacketBuilder.createChatPacket(message);
        return state.ws()
                .sendText(packet, true)
                .orTimeout(config.getConnectionTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .thenRun(() -> {});
    }

    public CompletableFuture<Void> sendEnterInfo(String synAck) {
        WsState state = wsState.get();
        if (!state.connected() || state.ws() == null) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("WebSocket is not connected"));
        }
        LOGGER.fine("Sending ENTER_INFO packet...");
        String packet = WebSocketPacketBuilder.createEnterInfoPacket(synAck);
        return state.ws().sendText(packet, true).thenRun(() -> {});
    }

    public CompletableFuture<Void> getReconnectFuture() {
        return reconnectFuture.get();
    }
}
