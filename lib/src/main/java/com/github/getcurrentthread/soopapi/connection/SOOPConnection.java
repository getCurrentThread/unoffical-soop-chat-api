package com.github.getcurrentthread.soopapi.connection;

import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.getcurrentthread.soopapi.config.SOOPChatConfig;
import com.github.getcurrentthread.soopapi.decoder.MessageDispatcher;
import com.github.getcurrentthread.soopapi.decoder.factory.DefaultMessageDecoderFactory;
import com.github.getcurrentthread.soopapi.event.EventEmitter;
import com.github.getcurrentthread.soopapi.exception.ConnectionException;
import com.github.getcurrentthread.soopapi.model.ChannelInfo;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;
import com.github.getcurrentthread.soopapi.websocket.WebSocketListener;
import com.github.getcurrentthread.soopapi.websocket.WebSocketManager;

public class SOOPConnection {
    private static final Logger LOGGER = Logger.getLogger(SOOPConnection.class.getName());

    private final SOOPChatConfig config;
    private final MessageDispatcher messageDispatcher;
    private final WebSocketManager webSocketManager;

    private ChannelInfo channelInfo;
    private volatile boolean isConnected;
    private volatile boolean isReconnecting;
    private final Object connectionLock = new Object();

    public SOOPConnection(
            SOOPChatConfig config,
            ExecutorService messageProcessor,
            ScheduledExecutorService scheduler,
            EventEmitter eventEmitter) {
        this.config = config;

        this.messageDispatcher =
                new MessageDispatcher(
                        new DefaultMessageDecoderFactory().createDecoders(),
                        messageProcessor,
                        eventEmitter);

        WebSocketListener listener = new WebSocketListener(messageDispatcher);
        this.webSocketManager =
                new WebSocketManager(config, config.getSSLContext(), scheduler, listener);
    }

    public CompletableFuture<Void> connect() {
        return CompletableFuture.runAsync(
                () -> {
                    synchronized (connectionLock) {
                        if (isConnected) {
                            LOGGER.info("이미 연결되어 있습니다.");
                            return;
                        }

                        try {
                            LOGGER.info("채널 정보 가져오는 중: " + config.getBid());
                            String bno =
                                    config.getBno() != null
                                            ? config.getBno()
                                            : SOOPChatUtils.getBnoFromBid(config.getBid());

                            channelInfo = SOOPChatUtils.getPlayerLive(bno, config.getBid());
                            LOGGER.info("채널 정보 수신됨: " + channelInfo);

                            if (channelInfo.CHPT == null || channelInfo.CHPT.trim().isEmpty()) {
                                throw new ConnectionException(
                                        "채널 포트 정보가 유효하지 않습니다: " + channelInfo.CHPT);
                            }

                            if (channelInfo.CHDOMAIN == null
                                    || channelInfo.CHDOMAIN.trim().isEmpty()) {
                                throw new ConnectionException(
                                        "채널 도메인 정보가 유효하지 않습니다: " + channelInfo.CHDOMAIN);
                            }

                            int maxTries = 5;
                            for (int i = 0; i < maxTries; i++) {
                                try {
                                    webSocketManager.connect(channelInfo).join();
                                    isConnected = true;
                                    break;
                                } catch (Exception e) {
                                    if (i == maxTries - 1) {
                                        throw e;
                                    }
                                    LOGGER.log(
                                            Level.WARNING,
                                            "연결 시도 " + (i + 1) + "/" + maxTries + " 실패, 재시도 중...",
                                            e);
                                    Thread.sleep(1000);
                                }
                            }

                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "연결 실패", e);
                            throw new CompletionException(new ConnectionException("연결에 실패했습니다", e));
                        }
                    }
                });
    }

    public CompletableFuture<Void> reconnect() {
        return CompletableFuture.runAsync(
                () -> {
                    synchronized (connectionLock) {
                        if (isReconnecting) {
                            LOGGER.info("이미 재연결 중입니다.");
                            if (webSocketManager.getReconnectFuture() != null) {
                                try {
                                    webSocketManager.getReconnectFuture().join();
                                } catch (Exception e) {
                                    LOGGER.log(Level.WARNING, "재연결 대기 중 오류 발생", e);
                                    throw new CompletionException(e);
                                }
                            }
                            return;
                        }

                        isReconnecting = true;
                        try {
                            LOGGER.info("재연결 시도 중...");
                            webSocketManager.reconnect().join();
                            isConnected = true;
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "재연결 실패", e);
                            throw new CompletionException(
                                    new ConnectionException("재연결에 실패했습니다", e));
                        } finally {
                            isReconnecting = false;
                        }
                    }
                });
    }

    public CompletableFuture<Void> sendChat(String message) {
        return webSocketManager.sendChat(message);
    }

    public void disconnect() {
        synchronized (connectionLock) {
            try {
                webSocketManager.disconnect();
            } finally {
                isConnected = false;
            }
        }
    }

    public CompletableFuture<ConnectionStatus> getStatus() {
        return webSocketManager
                .getStatus()
                .thenApply(
                        wsStatus ->
                                new ConnectionStatus(
                                        wsStatus.isConnected(),
                                        isReconnecting,
                                        wsStatus.getRetryCount()));
    }

    public static class ConnectionStatus {
        private final boolean connected;
        private final boolean reconnecting;
        private final int retryCount;

        public ConnectionStatus(boolean connected, boolean reconnecting, int retryCount) {
            this.connected = connected;
            this.reconnecting = reconnecting;
            this.retryCount = retryCount;
        }

        public boolean isConnected() {
            return connected;
        }

        public boolean isReconnecting() {
            return reconnecting;
        }

        public int getRetryCount() {
            return retryCount;
        }
    }

    public boolean isConnected() {
        return isConnected && webSocketManager.isConnected();
    }

    public boolean isReconnecting() {
        return isReconnecting;
    }

    public ChannelInfo getChannelInfo() {
        return channelInfo;
    }

    public SOOPChatConfig getConfig() {
        return config;
    }
}
