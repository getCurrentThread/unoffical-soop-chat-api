package com.github.getcurrentthread.soopapi.connection;

import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.getcurrentthread.soopapi.client.IChatMessageObserver;
import com.github.getcurrentthread.soopapi.config.SOOPChatConfig;
import com.github.getcurrentthread.soopapi.decoder.MessageDispatcher;
import com.github.getcurrentthread.soopapi.decoder.factory.DefaultMessageDecoderFactory;
import com.github.getcurrentthread.soopapi.exception.ConnectionException;
import com.github.getcurrentthread.soopapi.model.ChannelInfo;
import com.github.getcurrentthread.soopapi.model.Message;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;
import com.github.getcurrentthread.soopapi.websocket.WebSocketListener;
import com.github.getcurrentthread.soopapi.websocket.WebSocketManager;

public class SOOPConnection {
    private static final Logger LOGGER = Logger.getLogger(SOOPConnection.class.getName());

    private final SOOPChatConfig config;
    private final ExecutorService messageProcessor;
    private final ScheduledExecutorService scheduler;
    private final List<IChatMessageObserver> observers;
    private final MessageDispatcher messageDispatcher;
    private final WebSocketManager webSocketManager;

    private ChannelInfo channelInfo;
    private volatile boolean isConnected;
    private volatile boolean isReconnecting;
    private final Object connectionLock = new Object();

    public SOOPConnection(
            SOOPChatConfig config,
            ExecutorService messageProcessor,
            ScheduledExecutorService scheduler) {
        this.config = config;
        this.messageProcessor = messageProcessor;
        this.scheduler = scheduler;
        this.observers = new CopyOnWriteArrayList<>();

        // MessageDispatcher 초기화
        this.messageDispatcher =
                new MessageDispatcher(
                        new DefaultMessageDecoderFactory().createDecoders(), messageProcessor);

        // 메시지 핸들러 설정
        this.messageDispatcher.setMessageHandler(this::notifyObservers);

        // WebSocketManager 초기화
        WebSocketListener listener = new WebSocketListener(messageDispatcher);
        this.webSocketManager =
                new WebSocketManager(config, config.getSSLContext(), scheduler, listener);
    }

    /**
     * 서버에 연결합니다.
     *
     * @return 연결 작업을 나타내는 CompletableFuture
     */
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

                            // 연결 시도 전에 channelInfo의 CHPT가 유효한지 확인
                            if (channelInfo.CHPT == null || channelInfo.CHPT.trim().isEmpty()) {
                                throw new ConnectionException(
                                        "채널 포트 정보가 유효하지 않습니다: " + channelInfo.CHPT);
                            }

                            // CHDOMAIN이 유효한지 확인
                            if (channelInfo.CHDOMAIN == null
                                    || channelInfo.CHDOMAIN.trim().isEmpty()) {
                                throw new ConnectionException(
                                        "채널 도메인 정보가 유효하지 않습니다: " + channelInfo.CHDOMAIN);
                            }

                            // 최대 5번 시도, 1초 간격으로 재시도
                            int maxTries = 5;
                            for (int i = 0; i < maxTries; i++) {
                                try {
                                    webSocketManager.connect(channelInfo).join();
                                    isConnected = true;
                                    break;
                                } catch (Exception e) {
                                    if (i == maxTries - 1) { // 마지막 시도였다면
                                        throw e; // 예외를 다시 던짐
                                    }
                                    LOGGER.log(
                                            Level.WARNING,
                                            "연결 시도 " + (i + 1) + "/" + maxTries + " 실패, 재시도 중...",
                                            e);
                                    Thread.sleep(1000); // 1초 대기 후 재시도
                                }
                            }

                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "연결 실패", e);
                            throw new CompletionException(new ConnectionException("연결에 실패했습니다", e));
                        }
                    }
                },
                scheduler);
    }

    /**
     * 연결이 끊어졌을 때 자동으로 재연결을 시도합니다.
     *
     * @return 재연결 작업을 나타내는 CompletableFuture
     */
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
                },
                scheduler);
    }

    private void notifyObservers(Message message) {
        if (message == null) {
            LOGGER.warning("수신된 null 메시지");
            return;
        }

        LOGGER.info("메시지 브로드캐스팅: " + message);
        for (IChatMessageObserver observer : observers) {
            try {
                observer.notify(message);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "옵저버 알림 중 오류 발생", e);
            }
        }
    }

    /**
     * 새 메시지 옵저버를 추가합니다.
     *
     * @param observer 추가할 옵저버
     */
    public void addObserver(IChatMessageObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
            LOGGER.info("옵저버 추가됨, 총 옵저버: " + observers.size());
        }
    }

    /**
     * 메시지 옵저버를 제거합니다.
     *
     * @param observer 제거할 옵저버
     */
    public void removeObserver(IChatMessageObserver observer) {
        observers.remove(observer);
        LOGGER.info("옵저버 제거됨, 남은 옵저버: " + observers.size());
    }

    /** 현재 연결을 해제합니다. */
    public void disconnect() {
        synchronized (connectionLock) {
            try {
                webSocketManager.disconnect();
            } finally {
                isConnected = false;
                // 옵저버는 유지하여 재연결 시 사용할 수 있도록 함
            }
        }
    }

    /**
     * 현재 웹소켓 연결 상태를 가져옵니다.
     *
     * @return 연결 상태
     */
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

    /** 연결 상태 정보를 제공하는 클래스 */
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

    /**
     * 현재 연결 상태를 반환합니다.
     *
     * @return 연결 상태
     */
    public boolean isConnected() {
        return isConnected && webSocketManager.isConnected();
    }

    /**
     * 현재 재연결 중인지 여부를 반환합니다.
     *
     * @return 재연결 상태
     */
    public boolean isReconnecting() {
        return isReconnecting;
    }

    /**
     * 채널 정보를 반환합니다.
     *
     * @return 채널 정보 또는 null (연결되지 않은 경우)
     */
    public ChannelInfo getChannelInfo() {
        return channelInfo;
    }

    /**
     * 설정 정보를 반환합니다.
     *
     * @return 설정 정보
     */
    public SOOPChatConfig getConfig() {
        return config;
    }
}
