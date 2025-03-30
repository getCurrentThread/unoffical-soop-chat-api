package com.github.getcurrentthread.soopapi.client;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.getcurrentthread.soopapi.config.SOOPChatConfig;
import com.github.getcurrentthread.soopapi.connection.ConnectionManager;
import com.github.getcurrentthread.soopapi.model.Message;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class SOOPChatClient implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(SOOPChatClient.class.getName());

    private final SOOPChatConfig config;
    private final ConnectionManager connectionManager;
    private final List<IChatMessageObserver> observers;
    private volatile boolean isConnected;

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
        // 가상 스레드를 사용하여 각 옵저버에게 메시지 통지
        for (IChatMessageObserver observer : observers) {
            CompletableFuture.runAsync(
                    () -> {
                        try {
                            observer.notify(message);
                        } catch (Exception e) {
                            LOGGER.log(Level.WARNING, "Error notifying observer", e);
                        }
                    },
                    Executors.newVirtualThreadPerTaskExecutor());
        }
    }

    public CompletableFuture<Void> connectToChat() {
        if (isConnected) {
            return CompletableFuture.completedFuture(null);
        }

        // 가상 스레드를 사용하여 연결 로직 수행
        return CompletableFuture.runAsync(
                () -> {
                    try {
                        connectionManager
                                .connect(config, this::notifyObservers)
                                .thenRun(() -> isConnected = true)
                                .exceptionally(
                                        throwable -> {
                                            LOGGER.log(
                                                    Level.SEVERE, "Failed to connect", throwable);
                                            return null;
                                        })
                                .join();
                    } catch (Exception e) {
                        throw new CompletionException("Failed to connect to chat", e);
                    }
                },
                Executors.newVirtualThreadPerTaskExecutor());
    }

    public void disconnect() {
        if (!isConnected) {
            return;
        }

        try {
            connectionManager.disconnect(config.getBid());
            isConnected = false;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error during disconnect", e);
        }
    }

    @Override
    public void close() {
        disconnect();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String getBid() {
        return config.getBid();
    }
}
