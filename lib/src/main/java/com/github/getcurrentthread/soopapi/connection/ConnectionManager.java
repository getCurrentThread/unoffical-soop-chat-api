package com.github.getcurrentthread.soopapi.connection;

import com.github.getcurrentthread.soopapi.client.IChatMessageObserver;
import com.github.getcurrentthread.soopapi.config.SOOPChatConfig;

import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class ConnectionManager {
    private static final Logger LOGGER = Logger.getLogger(ConnectionManager.class.getName());
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 2;

    private final ExecutorService messageProcessorPool;
    private final ScheduledExecutorService sharedScheduler;
    private final Map<String, SOOPConnection> connections;

    private static class InstanceHolder {
        private static final ConnectionManager INSTANCE = new ConnectionManager();
    }

    private ConnectionManager() {
        this.messageProcessorPool = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(5000),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        this.sharedScheduler = Executors.newScheduledThreadPool(2);
        this.connections = new ConcurrentHashMap<>();
    }

    public static ConnectionManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public CompletableFuture<SOOPConnection> connect(SOOPChatConfig config, IChatMessageObserver observer) {
        String bid = config.getBid();

        return CompletableFuture.supplyAsync(() -> {
            SOOPConnection connection = connections.computeIfAbsent(bid,
                    k -> new SOOPConnection(config, messageProcessorPool, sharedScheduler));

            connection.addObserver(observer);
            connection.connect().join();
            return connection;
        });
    }

    public void disconnect(String bid) {
        SOOPConnection connection = connections.remove(bid);
        if (connection != null) {
            connection.disconnect();
        }
    }

    public void shutdown() {
        connections.values().forEach(SOOPConnection::disconnect);
        connections.clear();

        messageProcessorPool.shutdown();
        sharedScheduler.shutdown();

        try {
            if (!messageProcessorPool.awaitTermination(5, TimeUnit.SECONDS)) {
                messageProcessorPool.shutdownNow();
            }
            if (!sharedScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                sharedScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}