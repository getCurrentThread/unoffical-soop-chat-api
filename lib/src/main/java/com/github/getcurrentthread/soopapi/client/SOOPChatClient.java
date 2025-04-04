package com.github.getcurrentthread.soopapi.client;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.getcurrentthread.soopapi.config.SOOPChatConfig;
import com.github.getcurrentthread.soopapi.connection.ConnectionManager;
import com.github.getcurrentthread.soopapi.connection.SOOPConnection;
import com.github.getcurrentthread.soopapi.exception.ConnectionException;
import com.github.getcurrentthread.soopapi.model.Message;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class SOOPChatClient implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(SOOPChatClient.class.getName());

    private final SOOPChatConfig config;
    private final ConnectionManager connectionManager;
    private final List<IChatMessageObserver> observers;
    private volatile boolean isConnected;
    private volatile SOOPConnection connection;

    public SOOPChatClient(SOOPChatConfig config) {
        this.config = validateConfig(config);
        this.connectionManager = ConnectionManager.getInstance();
        this.observers = new CopyOnWriteArrayList<>();
    }

    private SOOPChatConfig validateConfig(SOOPChatConfig config) {
        if (config.getBno() == null) {
            String bno = SOOPChatUtils.getBnoFromBid(config.getBid());
            return new SOOPChatConfig.Builder()
                    .bid(config.getBid())
                    .bno(bno)
                    .sslContext(config.getSSLContext())
                    .build();
        }
        return config;
    }

    public void addObserver(IChatMessageObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(IChatMessageObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(Message message) {
        for (IChatMessageObserver observer : observers) {
            try {
                observer.notify(message);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error notifying observer", e);
            }
        }
    }

    /**
     * 채팅에 비동기적으로 연결합니다.
     *
     * @return 연결 작업을 나타내는 CompletableFuture
     */
    public CompletableFuture<Void> connectToChat() {
        if (isConnected) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(
                () -> {
                    try {
                        connection =
                                connectionManager.connect(config, this::notifyObservers).join();
                        isConnected = true;
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "채팅 연결 실패", e);
                        if (e.getCause() instanceof ConnectionException) {
                            throw new CompletionException(e.getCause());
                        } else {
                            throw new CompletionException("채팅 연결 실패", e);
                        }
                    }
                });
    }

    /**
     * 채팅 연결을 시도하고 완료될 때까지 현재 스레드를 차단합니다.
     *
     * @throws ConnectionException 연결 오류가 발생한 경우
     */
    public void connectToChattingBlocking() throws ConnectionException {
        try {
            connectToChat().join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof ConnectionException) {
                throw (ConnectionException) e.getCause();
            } else {
                throw new ConnectionException("채팅 연결 실패", e);
            }
        }
    }

    /**
     * 연결이 끊어진 경우 재연결을 시도합니다.
     *
     * @return 재연결 작업을 나타내는 CompletableFuture
     */
    public CompletableFuture<Void> reconnect() {
        if (connection == null) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("연결이 초기화되지 않았습니다. 먼저 connectToChat을 호출하세요."));
        }

        return connection
                .reconnect()
                .thenRun(() -> isConnected = true)
                .exceptionally(
                        e -> {
                            isConnected = false;
                            LOGGER.log(Level.SEVERE, "재연결 실패", e);
                            throw new CompletionException(e);
                        });
    }

    /**
     * 현재 연결 상태를 확인합니다.
     *
     * @return 연결 상태 정보를 포함하는 CompletableFuture
     */
    public CompletableFuture<ConnectionStatus> getConnectionStatus() {
        if (connection == null) {
            return CompletableFuture.completedFuture(new ConnectionStatus(false, false, 0));
        }

        return connection
                .getStatus()
                .thenApply(
                        status ->
                                new ConnectionStatus(
                                        status.isConnected(),
                                        status.isReconnecting(),
                                        status.getRetryCount()));
    }

    /** 현재 연결을 해제합니다. */
    public void disconnect() {
        if (!isConnected) {
            return;
        }

        try {
            connectionManager.disconnect(config.getBid()).join();
            isConnected = false;
            connection = null;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error during disconnect", e);
        }
    }

    @Override
    public void close() {
        disconnect();
    }

    /**
     * 현재 연결 상태를 반환합니다.
     *
     * @return 연결 상태
     */
    public boolean isConnected() {
        return isConnected && (connection != null && connection.isConnected());
    }

    /**
     * 방송인 ID를 반환합니다.
     *
     * @return 방송인 ID
     */
    public String getBid() {
        return config.getBid();
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
}
