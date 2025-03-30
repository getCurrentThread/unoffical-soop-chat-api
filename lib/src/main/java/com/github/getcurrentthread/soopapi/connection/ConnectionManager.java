package com.github.getcurrentthread.soopapi.connection;

import com.github.getcurrentthread.soopapi.client.IChatMessageObserver;
import com.github.getcurrentthread.soopapi.config.SOOPChatConfig;

import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class ConnectionManager {
    private static final Logger LOGGER = Logger.getLogger(ConnectionManager.class.getName());

    // 가상 스레드 기반 ExecutorService로 변경
    private final ExecutorService messageProcessorPool;
    private final ScheduledExecutorService sharedScheduler;
    private final Map<String, SOOPConnection> connections;

    private static class InstanceHolder {
        private static final ConnectionManager INSTANCE = new ConnectionManager();
    }

    private ConnectionManager() {
        // 가상 스레드를 사용하는 ExecutorService 생성
        this.messageProcessorPool = Executors.newVirtualThreadPerTaskExecutor();

        // 타이머 작업을 위한 스케줄러는 유지 (가상 스레드는 ScheduledExecutorService를 구현하지 않음)
        this.sharedScheduler = Executors.newScheduledThreadPool(2,
                Thread.ofVirtual().name("scheduler-", 0).factory());

        this.connections = new ConcurrentHashMap<>();
    }

    public static ConnectionManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public CompletableFuture<SOOPConnection> connect(SOOPChatConfig config, IChatMessageObserver observer) {
        String bid = config.getBid();

        // 가상 스레드로 비동기 작업 실행
        return CompletableFuture.supplyAsync(() -> {
            SOOPConnection connection = connections.computeIfAbsent(bid,
                    k -> new SOOPConnection(config, messageProcessorPool, sharedScheduler));

            connection.addObserver(observer);
            connection.connect().join();
            return connection;
        }, messageProcessorPool);
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

        // 스레드 풀 정리
        messageProcessorPool.close();
        sharedScheduler.shutdown();

        try {
            if (!sharedScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                sharedScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}