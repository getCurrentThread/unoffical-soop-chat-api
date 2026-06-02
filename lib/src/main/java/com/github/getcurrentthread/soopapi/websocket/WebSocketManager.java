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

    private record WsState(WebSocket ws, ScheduledFuture<?> ping) {
        static final WsState EMPTY = new WsState(null, null);

        boolean isConnected() {
            return ws != null;
        }

        WsState withPing(ScheduledFuture<?> p) {
            return new WsState(ws, p);
        }
    }

    private final SOOPChatConfig config;
    private final HttpClient httpClient;
    private final ScheduledExecutorService scheduler;
    private final WebSocketListener listener;
    private final EventEmitter eventEmitter;
    private final AtomicReference<WsState> wsState = new AtomicReference<>(WsState.EMPTY);
    private final AtomicInteger retryCount = new AtomicInteger(0);
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

        SSLContext ssl = sslContext != null ? sslContext : SSLContextProvider.getInstance();
        this.httpClient =
                HttpClient.newBuilder()
                        .sslContext(ssl)
                        .connectTimeout(config.getConnectionTimeout())
                        .build();
        LOGGER.fine("WebSocketManager initialized");
    }

    /**
     * WebSocket에 연결합니다. 실패 시 자동 재시도가 적용됩니다.
     *
     * @param channelInfo 연결할 채널 정보
     * @return 연결이 수립되면 완료되는 CompletableFuture
     */
    public CompletableFuture<Void> connect(ChannelInfo channelInfo) {
        LOGGER.fine(
                () ->
                        "Attempting to connect to WebSocket: "
                                + channelInfo.CHDOMAIN()
                                + ":"
                                + channelInfo.CHPT());

        CompletableFuture<Void> connectionFuture = new CompletableFuture<>();
        attemptConnect(channelInfo)
                .whenComplete(
                        (unused, err) -> {
                            if (err == null) {
                                connectionFuture.complete(null);
                            } else {
                                LOGGER.log(
                                        Level.SEVERE,
                                        "Failed to establish WebSocket connection",
                                        err);
                                scheduleRetry(channelInfo, err, connectionFuture);
                            }
                        });
        return connectionFuture;
    }

    /** 재시도 없는 단일 연결 시도. {@link #connect(ChannelInfo)}와 {@link #scheduleRetry}가 공유한다. */
    private CompletableFuture<Void> attemptConnect(ChannelInfo channelInfo) {
        currentChannelInfo.set(channelInfo);
        CompletableFuture<Void> done = new CompletableFuture<>();
        openSocket(channelInfo)
                .whenComplete(
                        (ws, err) -> {
                            if (err != null) {
                                done.completeExceptionally(err);
                            } else {
                                handleConnectionSuccess(ws, channelInfo, done);
                            }
                        });
        return done;
    }

    private CompletableFuture<WebSocket> openSocket(ChannelInfo channelInfo) {
        try {
            var uri = buildWebSocketUri(channelInfo);
            LOGGER.fine(() -> "Connecting to URI: " + uri);
            return httpClient
                    .newWebSocketBuilder()
                    .subprotocols("chat")
                    .connectTimeout(config.getConnectionTimeout())
                    .buildAsync(uri, listener);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
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

        return requireActiveSocket()
                .thenCompose(ws -> ws.sendText(connectPacket, true))
                .thenCompose(
                        _ -> {
                            LOGGER.info("Connect packet sent successfully, sending join packet...");
                            return requireActiveSocket()
                                    .thenCompose(ws -> ws.sendText(joinPacket, true));
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
                () ->
                        "Attempting retry %d of %d in %dms"
                                .formatted(currentRetry, config.getMaxRetryAttempts(), delay));

        emitReconnecting(currentRetry, delay);

        var newFuture = new CompletableFuture<Void>();
        reconnectFuture.set(newFuture);

        scheduler.schedule(
                () -> runScheduledRetry(channelInfo, connectionFuture, currentRetry, newFuture),
                delay,
                TimeUnit.MILLISECONDS);
    }

    private void runScheduledRetry(
            ChannelInfo channelInfo,
            CompletableFuture<Void> connectionFuture,
            int currentRetry,
            CompletableFuture<Void> rf) {
        try {
            attemptConnect(channelInfo)
                    .thenRun(
                            () -> {
                                rf.complete(null);
                                emitReconnected(currentRetry);
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
            rf.completeExceptionally(e);
        }
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
        LOGGER.info("WebSocket connection established");
        wsState.set(new WsState(ws, null));
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
        var current = wsState.get();
        if (current.ping() != null && !current.ping().isDone()) {
            current.ping().cancel(false);
        }

        long pingInterval = config.getPingIntervalSeconds();
        String pingPacket = WebSocketPacketBuilder.createPingPacket();
        var newPingTask =
                scheduler.scheduleWithFixedDelay(
                        () -> tickPing(pingPacket), pingInterval, pingInterval, TimeUnit.SECONDS);

        // 새로운 ping 작업으로 상태를 원자적으로 업데이트하며, 정리(cleanup) 경쟁 상태를 확인
        wsState.updateAndGet(
                s -> {
                    if (!s.isConnected()) {
                        newPingTask.cancel(false);
                        return s;
                    }
                    return s.withPing(newPingTask);
                });
    }

    private void tickPing(String pingPacket) {
        var state = wsState.get();
        if (!state.isConnected()) {
            return;
        }
        try {
            LOGGER.fine("Sending ping packet");
            state.ws().sendText(pingPacket, true).join();
        } catch (Exception e) {
            handlePingFailure(e);
        }
    }

    private void handlePingFailure(Exception cause) {
        LOGGER.log(Level.WARNING, "Error sending ping", cause);
        if (!wsState.get().isConnected()) {
            return;
        }
        cleanupResources();
        LOGGER.fine("Ping failed, attempting to reconnect");
        int attempt = Math.max(1, retryCount.get());
        emitReconnecting(attempt, calculateExponentialBackoff(attempt));
        reconnect()
                .thenRun(() -> emitReconnected(retryCount.get()))
                .exceptionally(
                        ex -> {
                            LOGGER.log(Level.SEVERE, "Ping-triggered reconnect failed", ex);
                            return null;
                        });
    }

    /**
     * 현재 채널 정보를 사용하여 연결을 재시도합니다.
     *
     * @return 재연결이 성공하면 완료되는 CompletableFuture
     */
    public CompletableFuture<Void> reconnect() {
        var channelInfo = currentChannelInfo.get();
        if (channelInfo == null) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException(
                            "No previous channel info available for reconnection"));
        }

        var existing = reconnectFuture.get();
        if (existing != null && !existing.isDone()) {
            return existing;
        }

        cleanupResources();
        return connect(channelInfo);
    }

    public void disconnect() {
        var state = wsState.get();
        if (state.isConnected()) {
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
        var old = wsState.getAndSet(WsState.EMPTY);
        if (old.ping() != null) {
            old.ping().cancel(false);
        }
    }

    private void emitReconnecting(int attempt, long delayMs) {
        eventEmitter.emit(
                ChatEvent.RECONNECTING,
                new ReconnectingEvent(
                        attempt,
                        config.getMaxRetryAttempts(),
                        delayMs,
                        ChatEvent.RECONNECTING,
                        "",
                        System.currentTimeMillis()));
    }

    private void emitReconnected(int attempt) {
        eventEmitter.emit(
                ChatEvent.RECONNECTED,
                new ReconnectedEvent(
                        attempt, ChatEvent.RECONNECTED, "", System.currentTimeMillis()));
    }

    /**
     * 현재 WebSocket 연결 상태를 반환합니다.
     *
     * @return 연결 상태
     */
    public CompletableFuture<WebSocketStatus> getStatus() {
        boolean connected = isConnected();
        return CompletableFuture.completedFuture(
                new WebSocketStatus(
                        connected, connected ? 0 : retryCount.get(), config.getMaxRetryAttempts()));
    }

    public record WebSocketStatus(boolean connected, int retryCount, int maxRetries) {}

    @Override
    public void close() {
        disconnect();
        httpClient.close();
    }

    public boolean isConnected() {
        return wsState.get().isConnected();
    }

    private CompletableFuture<WebSocket> requireActiveSocket() {
        var state = wsState.get();
        return state.isConnected()
                ? CompletableFuture.completedFuture(state.ws())
                : CompletableFuture.failedFuture(
                        new IllegalStateException("WebSocket is not connected"));
    }

    public CompletableFuture<Void> sendChat(String message) {
        String packet = WebSocketPacketBuilder.createChatPacket(message);
        return requireActiveSocket()
                .thenCompose(
                        ws ->
                                ws.sendText(packet, true)
                                        .orTimeout(
                                                config.getConnectionTimeout().toMillis(),
                                                TimeUnit.MILLISECONDS))
                .thenRun(() -> {});
    }

    public CompletableFuture<Void> sendWhisper(String targetId, String message) {
        String packet = WebSocketPacketBuilder.createWhisperPacket(targetId, message);
        return requireActiveSocket()
                .thenCompose(
                        ws ->
                                ws.sendText(packet, true)
                                        .orTimeout(
                                                config.getConnectionTimeout().toMillis(),
                                                TimeUnit.MILLISECONDS))
                .thenRun(() -> {});
    }

    public CompletableFuture<Void> sendEnterInfo(String synAck) {
        LOGGER.fine("Sending ENTER_INFO packet...");
        String packet = WebSocketPacketBuilder.createEnterInfoPacket(synAck);
        return requireActiveSocket().thenCompose(ws -> ws.sendText(packet, true)).thenRun(() -> {});
    }

    public CompletableFuture<Void> getReconnectFuture() {
        return reconnectFuture.get();
    }
}
