package com.github.getcurrentthread.soopapi.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.getcurrentthread.soopapi.config.SOOPChatConfig;
import com.github.getcurrentthread.soopapi.connection.ConnectionManager;
import com.github.getcurrentthread.soopapi.connection.SOOPConnection;
import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.EventEmitter;
import com.github.getcurrentthread.soopapi.event.EventListener;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.exception.ConnectionException;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class SOOPChatClient implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(SOOPChatClient.class.getName());

    private final SOOPChatConfig config;
    private final ConnectionManager connectionManager;
    private final EventEmitter eventEmitter;
    private volatile boolean isConnected;
    private volatile SOOPConnection connection;

    public SOOPChatClient(SOOPChatConfig config) {
        this.config = validateConfig(config);
        this.connectionManager = ConnectionManager.getInstance();
        this.eventEmitter = new EventEmitter();
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

    public <T extends BaseEvent> SOOPChatClient on(ChatEvent event, EventListener<T> listener) {
        eventEmitter.on(event, listener);
        return this;
    }

    public <T extends BaseEvent> SOOPChatClient once(ChatEvent event, EventListener<T> listener) {
        eventEmitter.once(event, listener);
        return this;
    }

    public <T extends BaseEvent> SOOPChatClient off(ChatEvent event, EventListener<T> listener) {
        eventEmitter.off(event, listener);
        return this;
    }

    public CompletableFuture<Void> connectToChat() {
        if (isConnected) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(
                () -> {
                    try {
                        connection = connectionManager.connect(config, eventEmitter).join();
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

    public CompletableFuture<Void> sendChat(String message) {
        if (connection == null || !isConnected) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("연결이 되어 있지 않습니다. 먼저 connectToChat을 호출하세요."));
        }
        return connection.sendChat(message);
    }

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

    public void disconnect() {
        if (!isConnected) {
            return;
        }

        try {
            connectionManager.disconnect(config.getBid()).join();
            isConnected = false;
            connection = null;
            eventEmitter.clear();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error during disconnect", e);
        }
    }

    @Override
    public void close() {
        disconnect();
    }

    public boolean isConnected() {
        return isConnected && (connection != null && connection.isConnected());
    }

    public String getBid() {
        return config.getBid();
    }

    public EventEmitter getEventEmitter() {
        return eventEmitter;
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
}
