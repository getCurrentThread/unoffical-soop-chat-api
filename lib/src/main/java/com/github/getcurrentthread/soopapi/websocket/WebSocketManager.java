package com.github.getcurrentthread.soopapi.websocket;

import com.github.getcurrentthread.soopapi.config.SOOPChatConfig;
import com.github.getcurrentthread.soopapi.model.ChannelInfo;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;
import com.github.getcurrentthread.soopapi.util.SSLContextProvider;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebSocketManager implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(WebSocketManager.class.getName());
    private static final long PING_INTERVAL_SECONDS = 60;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 5000;
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(30);

    private final SOOPChatConfig config;
    private final SSLContext sslContext;
    private final ScheduledExecutorService scheduler;
    private final WebSocketListener listener;
    private final AtomicBoolean isConnected;
    private final AtomicInteger retryCount;
    private volatile WebSocket webSocket;
    private volatile ScheduledFuture<?> pingTask;

    public WebSocketManager(SOOPChatConfig config, 
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

    public CompletableFuture<Void> connect(ChannelInfo channelInfo) {
        String wsUrl = String.format("wss://%s:%s/Websocket/%s", 
            channelInfo.CHDOMAIN, channelInfo.CHPT, config.getBid());
        LOGGER.info("Attempting to connect to WebSocket URL: " + wsUrl);

        CompletableFuture<Void> connectionFuture = new CompletableFuture<>();
            
        try {
            LOGGER.info("Creating HttpClient with SSL context: " + sslContext);
            // HTTP 클라이언트 생성 시 가상 스레드를 활용하도록 설정
            HttpClient client = HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .connectTimeout(CONNECT_TIMEOUT)
                    .executor(Executors.newVirtualThreadPerTaskExecutor()) // 가상 스레드 실행기 사용
                    .build();

            client.newWebSocketBuilder()
                    .subprotocols("chat")
                    .connectTimeout(CONNECT_TIMEOUT)
                    .buildAsync(URI.create(wsUrl), listener)
                    .thenAccept(ws -> {
                        try {
                            LOGGER.info("WebSocket connection established");
                            this.webSocket = ws;
                            isConnected.set(true);
                            retryCount.set(0);

                            // 가상 스레드에서 초기 패킷 전송 처리
                            sendInitialPackets(channelInfo)
                                    .thenRun(() -> {
                                        startPingScheduler();
                                        connectionFuture.complete(null);
                                    })
                                    .exceptionally(e -> {
                                        LOGGER.log(Level.SEVERE, "Failed to send initial packets", e);
                                        connectionFuture.completeExceptionally(e);
                                        return null;
                                    });
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "Error during connection setup", e);
                            connectionFuture.completeExceptionally(e);
                        }
                    })
                    .exceptionally(throwable -> {
                        LOGGER.log(Level.SEVERE, "Failed to establish WebSocket connection", throwable);
                        handleConnectionError(channelInfo, throwable);
                        connectionFuture.completeExceptionally(throwable);
                        return null;
                    });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initiating WebSocket connection", e);
            connectionFuture.completeExceptionally(e);
        }

        return connectionFuture;
    }

    private CompletableFuture<Void> sendInitialPackets(ChannelInfo channelInfo) {
        // 가상 스레드에서 실행되도록 변경
        return CompletableFuture.runAsync(() -> {
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
        }, Executors.newVirtualThreadPerTaskExecutor()); // 가상 스레드로 실행
    }

    private void handleConnectionError(ChannelInfo channelInfo, Throwable throwable) {
        LOGGER.log(Level.WARNING, "WebSocket connection error", throwable);
        
        if (retryCount.incrementAndGet() <= MAX_RETRY_ATTEMPTS) {
            LOGGER.info("Attempting retry " + retryCount.get() + " of " + MAX_RETRY_ATTEMPTS);
            scheduler.schedule(() -> {
                // 가상 스레드로 재시도 로직 실행
                CompletableFuture.runAsync(() -> {
                    try {
                        connect(channelInfo).join();
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Retry attempt failed", e);
                    }
                }, Executors.newVirtualThreadPerTaskExecutor());
            }, RETRY_DELAY_MS, TimeUnit.MILLISECONDS);
        } else {
            LOGGER.severe("Max retry attempts reached. Connection failed permanently.");
        }
    }

    private void startPingScheduler() {
        if (pingTask != null && !pingTask.isDone()) {
            pingTask.cancel(false);
        }

        String pingPacket = WebSocketPacketBuilder.createPingPacket();
        pingTask = scheduler.scheduleAtFixedRate(() -> {
            if (isConnected() && webSocket != null) {
                try {
                    LOGGER.fine("Sending ping packet");
                    webSocket.sendText(pingPacket, true).join();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error sending ping", e);
                }
            }
        }, PING_INTERVAL_SECONDS, PING_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

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

    @Override
    public void close() {
        disconnect();
    }

    public boolean isConnected() {
        return isConnected.get() && webSocket != null;
    }
}