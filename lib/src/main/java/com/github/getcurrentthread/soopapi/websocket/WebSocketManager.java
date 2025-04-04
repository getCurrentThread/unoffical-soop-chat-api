package com.github.getcurrentthread.soopapi.websocket;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import com.github.getcurrentthread.soopapi.config.SOOPChatConfig;
import com.github.getcurrentthread.soopapi.model.ChannelInfo;
import com.github.getcurrentthread.soopapi.util.SSLContextProvider;

public class WebSocketManager implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(WebSocketManager.class.getName());
    private static final long PING_INTERVAL_SECONDS = 60;
    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final long INITIAL_RETRY_DELAY_MS = 2000;
    private static final long MAX_RETRY_DELAY_MS = 30000;
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(30);

    private final SOOPChatConfig config;
    private final SSLContext sslContext;
    private final ScheduledExecutorService scheduler;
    private final WebSocketListener listener;
    private final AtomicBoolean isConnected;
    private final AtomicInteger retryCount;
    private final AtomicReference<ChannelInfo> currentChannelInfo = new AtomicReference<>();

    private volatile WebSocket webSocket;
    private volatile ScheduledFuture<?> pingTask;
    private volatile CompletableFuture<Void> reconnectFuture;

    public WebSocketManager(
            SOOPChatConfig config,
            SSLContext sslContext,
            ScheduledExecutorService scheduler,
            WebSocketListener listener) {
        this.config = config;
        // SSLContext가 null이면 기본 Provider에서 가져옴
        this.sslContext = sslContext != null ? sslContext : SSLContextProvider.getInstance();
        this.scheduler = scheduler;
        this.listener = listener;
        this.isConnected = new AtomicBoolean(false);
        this.retryCount = new AtomicInteger(0);
        LOGGER.info("WebSocketManager initialized with SSL context: " + this.sslContext);
    }

    /**
     * 웹소켓에 연결합니다.
     *
     * @param channelInfo 연결할 채널 정보
     * @return 연결 완료를 나타내는 CompletableFuture
     */
    public CompletableFuture<Void> connect(ChannelInfo channelInfo) {
        currentChannelInfo.set(channelInfo);
        String wsUrl =
                String.format(
                        "wss://%s:%s/Websocket/%s",
                        channelInfo.CHDOMAIN, channelInfo.CHPT, config.getBid());
        LOGGER.info("Attempting to connect to WebSocket URL: " + wsUrl);

        CompletableFuture<Void> connectionFuture = new CompletableFuture<>();

        try {
            LOGGER.info("Creating HttpClient with SSL context: " + sslContext);
            HttpClient client =
                    HttpClient.newBuilder()
                            .sslContext(sslContext)
                            .connectTimeout(CONNECT_TIMEOUT)
                            .build();

            // 포트 확인 - 일부 서버는 문자열로 포트 번호를 반환할 수 있음
            int portNumber;
            try {
                portNumber = Integer.parseInt(channelInfo.CHPT);
            } catch (NumberFormatException e) {
                LOGGER.warning(
                        "Invalid port number format: " + channelInfo.CHPT + ", using default 8001");
                portNumber = 8001;
            }

            // URI 생성 시 명시적으로 스키마, 호스트, 포트 지정
            URI uri =
                    new URI(
                            "wss",
                            null,
                            channelInfo.CHDOMAIN,
                            portNumber,
                            "/Websocket/" + config.getBid(),
                            null,
                            null);
            LOGGER.info("Connecting to URI: " + uri);

            client.newWebSocketBuilder()
                    .subprotocols("chat")
                    .connectTimeout(CONNECT_TIMEOUT)
                    .buildAsync(uri, listener)
                    .thenAccept(
                            ws -> {
                                try {
                                    LOGGER.info("WebSocket connection established");
                                    this.webSocket = ws;
                                    isConnected.set(true);
                                    retryCount.set(0);

                                    sendInitialPackets(channelInfo)
                                            .thenRun(
                                                    () -> {
                                                        startPingScheduler();
                                                        connectionFuture.complete(null);
                                                    })
                                            .exceptionally(
                                                    e -> {
                                                        LOGGER.log(
                                                                Level.SEVERE,
                                                                "Failed to send initial packets",
                                                                e);
                                                        connectionFuture.completeExceptionally(e);
                                                        return null;
                                                    });
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
                                handleConnectionError(channelInfo, throwable, connectionFuture);
                                return null;
                            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initiating WebSocket connection", e);
            connectionFuture.completeExceptionally(e);
        }

        return connectionFuture;
    }

    private CompletableFuture<Void> sendInitialPackets(ChannelInfo channelInfo) {
        return CompletableFuture.runAsync(
                () -> {
                    try {
                        LOGGER.info("Sending connect packet...");
                        String connectPacket = WebSocketPacketBuilder.createConnectPacket();
                        webSocket.sendText(connectPacket, true).join();
                        LOGGER.info("Connect packet sent successfully");

                        // 연결 안정화를 위한 짧은 대기
                        Thread.sleep(1000);

                        LOGGER.info("Sending join packet...");
                        String joinPacket = WebSocketPacketBuilder.createJoinPacket(channelInfo);
                        webSocket.sendText(joinPacket, true).join();
                        LOGGER.info("Join packet sent successfully");

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        LOGGER.log(Level.WARNING, "Interrupted while sending initial packets", e);
                        throw new CompletionException(e);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error sending initial packets", e);
                        throw new CompletionException(e);
                    }
                },
                scheduler);
    }

    private void handleConnectionError(
            ChannelInfo channelInfo,
            Throwable throwable,
            CompletableFuture<Void> connectionFuture) {
        LOGGER.log(Level.WARNING, "WebSocket connection error", throwable);

        int currentRetry = retryCount.incrementAndGet();

        if (currentRetry <= MAX_RETRY_ATTEMPTS) {
            long delay = calculateExponentialBackoff(currentRetry);

            LOGGER.info(
                    "Attempting retry "
                            + currentRetry
                            + " of "
                            + MAX_RETRY_ATTEMPTS
                            + " in "
                            + delay
                            + "ms");

            reconnectFuture = new CompletableFuture<>();

            scheduler.schedule(
                    () -> {
                        try {
                            connect(channelInfo)
                                    .thenRun(
                                            () -> {
                                                if (reconnectFuture != null) {
                                                    reconnectFuture.complete(null);
                                                }
                                                // 원래 연결 퓨처가 아직 완료되지 않았다면 완료시킵니다
                                                if (!connectionFuture.isDone()) {
                                                    connectionFuture.complete(null);
                                                }
                                            })
                                    .exceptionally(
                                            e -> {
                                                LOGGER.log(Level.SEVERE, "Retry attempt failed", e);
                                                // 원래 연결 퓨처가 아직 완료되지 않았다면 예외와 함께 완료시킵니다
                                                if (!connectionFuture.isDone()
                                                        && currentRetry == MAX_RETRY_ATTEMPTS) {
                                                    connectionFuture.completeExceptionally(e);
                                                }
                                                return null;
                                            });
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "Error during reconnection", e);
                            if (reconnectFuture != null) {
                                reconnectFuture.completeExceptionally(e);
                            }
                        }
                    },
                    delay,
                    TimeUnit.MILLISECONDS);
        } else {
            LOGGER.severe("Max retry attempts reached. Connection failed permanently.");
            // 원래 연결 퓨처가 아직 완료되지 않았다면 예외와 함께 완료시킵니다
            if (!connectionFuture.isDone()) {
                connectionFuture.completeExceptionally(throwable);
            }
        }
    }

    /** 지수 백오프를 사용하여 재시도 지연 시간을 계산합니다. */
    private long calculateExponentialBackoff(int retryCount) {
        long delay = INITIAL_RETRY_DELAY_MS * (long) Math.pow(2, retryCount - 1);
        return Math.min(delay, MAX_RETRY_DELAY_MS);
    }

    private void startPingScheduler() {
        if (pingTask != null && !pingTask.isDone()) {
            pingTask.cancel(false);
        }

        String pingPacket = WebSocketPacketBuilder.createPingPacket();
        pingTask =
                scheduler.scheduleAtFixedRate(
                        () -> {
                            if (isConnected() && webSocket != null) {
                                try {
                                    LOGGER.fine("Sending ping packet");
                                    webSocket.sendText(pingPacket, true).join();
                                } catch (Exception e) {
                                    LOGGER.log(Level.WARNING, "Error sending ping", e);
                                    // 핑 전송 중 오류가 발생하면 연결이 끊어진 것일 수 있으므로 자동 재연결을 시도합니다
                                    if (isConnected.get()) {
                                        LOGGER.info("Ping failed, attempting to reconnect");
                                        reconnect();
                                    }
                                }
                            }
                        },
                        PING_INTERVAL_SECONDS,
                        PING_INTERVAL_SECONDS,
                        TimeUnit.SECONDS);
    }

    /**
     * 현재 채널 정보를 사용하여 연결을 재시도합니다.
     *
     * @return 재연결 완료를 나타내는 CompletableFuture
     */
    public CompletableFuture<Void> reconnect() {
        ChannelInfo channelInfo = currentChannelInfo.get();
        if (channelInfo == null) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(
                    new IllegalStateException(
                            "No previous channel info available for reconnection"));
            return future;
        }

        cleanupResources();
        return connect(channelInfo);
    }

    /** 연결을 해제합니다. */
    public void disconnect() {
        if (webSocket != null) {
            try {
                LOGGER.info("Initiating WebSocket disconnect");
                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Disconnecting");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error during disconnect", e);
            } finally {
                cleanupResources();
            }
        }
    }

    private void cleanupResources() {
        LOGGER.info("Cleaning up WebSocket resources");
        isConnected.set(false);
        webSocket = null;
        if (pingTask != null) {
            pingTask.cancel(false);
            pingTask = null;
        }
    }

    /**
     * 현재 WebSocket 연결 상태를 반환합니다.
     *
     * @return 연결 상태
     */
    public CompletableFuture<WebSocketStatus> getStatus() {
        return CompletableFuture.supplyAsync(
                () -> {
                    if (!isConnected()) {
                        return new WebSocketStatus(false, retryCount.get(), MAX_RETRY_ATTEMPTS);
                    }
                    return new WebSocketStatus(true, 0, MAX_RETRY_ATTEMPTS);
                });
    }

    /** 웹소켓 상태 정보를 포함하는 클래스 */
    public static class WebSocketStatus {
        private final boolean connected;
        private final int retryCount;
        private final int maxRetries;

        public WebSocketStatus(boolean connected, int retryCount, int maxRetries) {
            this.connected = connected;
            this.retryCount = retryCount;
            this.maxRetries = maxRetries;
        }

        public boolean isConnected() {
            return connected;
        }

        public int getRetryCount() {
            return retryCount;
        }

        public int getMaxRetries() {
            return maxRetries;
        }
    }

    @Override
    public void close() {
        disconnect();
    }

    public boolean isConnected() {
        return isConnected.get() && webSocket != null;
    }

    /** 재연결 Future를 가져옵니다. 현재 재연결이 진행 중이 아니면 null을 반환합니다. */
    public CompletableFuture<Void> getReconnectFuture() {
        return reconnectFuture;
    }
}
