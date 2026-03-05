package com.github.getcurrentthread.soopapi.connection;

import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.getcurrentthread.soopapi.config.SOOPChatConfig;
import com.github.getcurrentthread.soopapi.event.EventEmitter;
import com.github.getcurrentthread.soopapi.exception.ConnectionException;
import com.github.getcurrentthread.soopapi.model.ConnectionStatus;

public class ConnectionManager {
    private static final Logger LOGGER = Logger.getLogger(ConnectionManager.class.getName());
    private static final StableValue<ConnectionManager> INSTANCE = StableValue.of();

    private final ExecutorService messageProcessorPool;
    private final ScheduledExecutorService sharedScheduler;
    private final Map<String, SOOPConnection> connections;
    private volatile boolean isShutdown;

    private ConnectionManager() {
        this.messageProcessorPool = Executors.newVirtualThreadPerTaskExecutor();

        this.sharedScheduler =
                Executors.newScheduledThreadPool(
                        1,
                        r -> {
                            Thread t = new Thread(r, "SOOP-Scheduler");
                            t.setDaemon(true);
                            return t;
                        });

        this.connections = new ConcurrentHashMap<>();

        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    for (SOOPConnection conn : connections.values()) {
                                        try {
                                            conn.disconnect();
                                        } catch (Exception e) {
                                            LOGGER.log(
                                                    Level.WARNING,
                                                    "Error disconnecting during shutdown",
                                                    e);
                                        }
                                    }
                                    connections.clear();
                                    shutdownExecutors();
                                },
                                "SOOP-ShutdownHook"));
    }

    public static ConnectionManager getInstance() {
        return INSTANCE.orElseSet(ConnectionManager::new);
    }

    public CompletableFuture<SOOPConnection> connect(
            SOOPChatConfig config, EventEmitter eventEmitter) {
        if (isShutdown) {
            return CompletableFuture.failedFuture(
                    new ConnectionException(
                            "ConnectionManager has been shut down and cannot accept new connections"));
        }
        String bid = config.getBid();

        return CompletableFuture.supplyAsync(
                () -> {
                    SOOPConnection connection =
                            connections.computeIfAbsent(
                                    bid,
                                    _ ->
                                            new SOOPConnection(
                                                    config,
                                                    messageProcessorPool,
                                                    sharedScheduler,
                                                    eventEmitter));
                    try {
                        connection.connect().join();
                        return connection;
                    } catch (Exception e) {
                        connections.computeIfPresent(
                                bid, (_, conn) -> conn.isConnected() ? conn : null);
                        LOGGER.log(Level.SEVERE, "Connection failed: " + bid, e);
                        throw new CompletionException(
                                new ConnectionException("Cannot connect to channel: " + bid, e));
                    }
                },
                messageProcessorPool);
    }

    public SOOPConnection getConnection(String bid) {
        return connections.get(bid);
    }

    public CompletableFuture<ConnectionStatus> getConnectionStatus(String bid) {
        SOOPConnection connection = connections.get(bid);
        if (connection == null) {
            return CompletableFuture.completedFuture(new ConnectionStatus(false, false, 0));
        }

        return connection
                .getStatus()
                .thenApply(
                        status ->
                                new ConnectionStatus(
                                        connection.isConnected(),
                                        status.reconnecting(),
                                        status.retryCount()));
    }

    public CompletableFuture<Void> disconnect(String bid) {
        return CompletableFuture.runAsync(
                () -> {
                    SOOPConnection connection = connections.remove(bid);
                    if (connection != null) {
                        connection.disconnect();
                    }
                },
                messageProcessorPool);
    }

    public CompletableFuture<Void> shutdown() {
        isShutdown = true;
        return CompletableFuture.runAsync(
                () -> {
                    try (var scope = StructuredTaskScope.open()) {
                        connections
                                .values()
                                .forEach(
                                        conn ->
                                                scope.fork(
                                                        () -> {
                                                            conn.disconnect();
                                                            return null;
                                                        }));
                        scope.join();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        LOGGER.log(Level.WARNING, "Interrupted during shutdown", e);
                    }
                    connections.clear();
                    shutdownExecutors();
                });
    }

    private void shutdownExecutors() {
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
            LOGGER.log(Level.WARNING, "Interrupted during shutdown", e);
        }
    }
}
