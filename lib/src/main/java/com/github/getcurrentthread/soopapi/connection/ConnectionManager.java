package com.github.getcurrentthread.soopapi.connection;

import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.getcurrentthread.soopapi.config.SOOPChatConfig;
import com.github.getcurrentthread.soopapi.event.EventEmitter;
import com.github.getcurrentthread.soopapi.exception.ConnectionException;

public class ConnectionManager {
    private static final Logger LOGGER = Logger.getLogger(ConnectionManager.class.getName());
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 2;
    private static final int KEEP_ALIVE_TIME = 60; // seconds

    private final ExecutorService messageProcessorPool;
    private final ScheduledExecutorService sharedScheduler;
    private final Map<String, SOOPConnection> connections;

    private static class InstanceHolder {
        private static final ConnectionManager INSTANCE = new ConnectionManager();
    }

    private ConnectionManager() {
        this.messageProcessorPool =
                new ThreadPoolExecutor(
                        CORE_POOL_SIZE,
                        MAX_POOL_SIZE,
                        KEEP_ALIVE_TIME,
                        TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>(5000),
                        new ThreadPoolExecutor.CallerRunsPolicy());

        this.sharedScheduler =
                Executors.newScheduledThreadPool(
                        2,
                        r -> {
                            Thread t = new Thread(r, "SOOP-Scheduler");
                            t.setDaemon(true);
                            return t;
                        });

        this.connections = new ConcurrentHashMap<>();
    }

    public static ConnectionManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public CompletableFuture<SOOPConnection> connect(
            SOOPChatConfig config, EventEmitter eventEmitter) {
        String bid = config.getBid();

        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        SOOPConnection connection =
                                connections.computeIfAbsent(
                                        bid,
                                        k ->
                                                new SOOPConnection(
                                                        config,
                                                        messageProcessorPool,
                                                        sharedScheduler,
                                                        eventEmitter));

                        connection.connect().join();
                        return connection;
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "연결 실패: " + bid, e);
                        throw new CompletionException(
                                new ConnectionException("채널에 연결할 수 없습니다: " + bid, e));
                    }
                });
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
                                        status.isReconnecting(),
                                        status.getRetryCount()));
    }

    public CompletableFuture<Void> disconnect(String bid) {
        return CompletableFuture.runAsync(
                () -> {
                    SOOPConnection connection = connections.remove(bid);
                    if (connection != null) {
                        connection.disconnect();
                    }
                });
    }

    public CompletableFuture<Void> shutdown() {
        return CompletableFuture.runAsync(
                () -> {
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
                        LOGGER.log(Level.WARNING, "종료 중 인터럽트 발생", e);
                    }
                });
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
