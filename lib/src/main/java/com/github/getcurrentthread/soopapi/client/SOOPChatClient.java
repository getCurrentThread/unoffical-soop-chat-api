package com.github.getcurrentthread.soopapi.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.getcurrentthread.soopapi.config.SOOPChatConfig;
import com.github.getcurrentthread.soopapi.connection.ConnectionManager;
import com.github.getcurrentthread.soopapi.connection.SOOPConnection;
import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.EventEmitter;
import com.github.getcurrentthread.soopapi.event.EventListener;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.DisconnectedEvent;
import com.github.getcurrentthread.soopapi.event.model.ReconnectedEvent;
import com.github.getcurrentthread.soopapi.event.model.ReconnectingEvent;
import com.github.getcurrentthread.soopapi.exception.AuthenticationException;
import com.github.getcurrentthread.soopapi.exception.ConnectionException;
import com.github.getcurrentthread.soopapi.model.ConnectionStatus;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

/**
 * SOOP 채팅 클라이언트.
 *
 * <p>{@link SOOPChatConfig}에 {@code authCookie}가 설정되지 않은 경우, 클라이언트는 익명(읽기 전용) 모드로 연결됩니다. 익명 모드에서는
 * 채팅 메시지를 수신할 수 있지만, {@link #sendChat(String)}을 호출하면 {@link AuthenticationException}이 발생합니다.
 */
public class SOOPChatClient implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(SOOPChatClient.class.getName());

    private final SOOPChatConfig config;
    private final ConnectionManager connectionManager;
    private final EventEmitter eventEmitter;
    private final ReentrantLock connectLock = new ReentrantLock();
    private volatile boolean isConnected;
    private volatile SOOPConnection connection;
    private volatile CompletableFuture<Void> disconnectFuture;

    public SOOPChatClient(SOOPChatConfig config) {
        this(config, ConnectionManager.getInstance());
    }

    SOOPChatClient(SOOPChatConfig config, ConnectionManager connectionManager) {
        validateConfig(config);
        this.config = config;
        this.connectionManager = connectionManager;
        this.eventEmitter = new EventEmitter();
    }

    private static void validateConfig(SOOPChatConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        if (config.getBid() == null || config.getBid().isBlank()) {
            throw new IllegalArgumentException("bid must not be null or blank");
        }
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

    /**
     * 비동기적으로 채팅에 연결합니다.
     *
     * <p>반환된 {@code CompletableFuture}는 연결이 <b>종료</b>될 때 완료됩니다. 따라서 {@code connectToChat().join()}을
     * 호출하면 연결이 종료될 때까지 현재 스레드가 블로킹됩니다.
     *
     * @return 연결 해제 시 완료되는 CompletableFuture
     */
    public CompletableFuture<Void> connectToChat() {
        connectLock.lock();
        try {
            if (isConnected || (disconnectFuture != null && !disconnectFuture.isDone())) {
                return disconnectFuture != null
                        ? disconnectFuture
                        : CompletableFuture.completedFuture(null);
            }

            disconnectFuture = new CompletableFuture<>();
            CompletableFuture<Void> df = disconnectFuture;

            eventEmitter.once(
                    ChatEvent.DISCONNECTED,
                    (DisconnectedEvent event) -> {
                        isConnected = false;
                        df.complete(null);
                    });

            Thread.startVirtualThread(
                    () -> {
                        try {
                            SOOPConnection conn =
                                    connectionManager.connect(config, eventEmitter).join();
                            connectLock.lock();
                            try {
                                connection = conn;
                                isConnected = true;
                            } finally {
                                connectLock.unlock();
                            }
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "Chat connection failed", e);
                            Throwable cause = SOOPChatUtils.unwrapCompletionException(e);
                            df.completeExceptionally(
                                    cause instanceof ConnectionException ce
                                            ? ce
                                            : new ConnectionException(
                                                    "Chat connection failed", cause));
                        }
                    });

            return df;
        } finally {
            connectLock.unlock();
        }
    }

    /**
     * 채팅에 연결하고 연결이 종료될 때까지 현재 스레드를 블로킹합니다.
     *
     * @throws ConnectionException 연결에 실패한 경우
     */
    public void connectAndAwait() throws ConnectionException {
        try {
            connectToChat().join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof ConnectionException ce) {
                throw ce;
            } else {
                throw new ConnectionException("Chat connection failed", e);
            }
        }
    }

    public CompletableFuture<Void> sendChat(String message) {
        if (!config.isAuthenticated()) {
            return CompletableFuture.failedFuture(
                    new AuthenticationException(
                            "Authentication required. Set AuthCookie to send chat messages."));
        }
        SOOPConnection conn = this.connection;
        if (conn == null || !isConnected) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("Not connected. Call connectToChat() first."));
        }
        return conn.sendChat(message);
    }

    /**
     * 현재 연결 위에서 가벼운 재연결을 수행합니다.
     *
     * <p>초기 연결이 형성되지 않았거나 자동 backoff 진행 중에 강제로 처음부터 다시 시작하려면 {@link #forceReconnect()}를 사용하세요.
     */
    public CompletableFuture<Void> reconnect() {
        SOOPConnection conn = this.connection;
        if (conn == null) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException(
                            "Connection not initialized. Call connectToChat() first."));
        }

        return conn.reconnect()
                .thenRun(() -> isConnected = true)
                .exceptionally(
                        e -> {
                            isConnected = false;
                            LOGGER.log(Level.SEVERE, "Reconnect failed", e);
                            throw new CompletionException(e);
                        });
    }

    /**
     * 현재 연결 상태와 무관하게 tear-down + 새 연결을 강제 실행합니다.
     *
     * <p>동작 순서:
     *
     * <ol>
     *   <li>{@link ChatEvent#RECONNECTING} 이벤트를 emit ({@code attemptNumber=1, maxAttempts=1,
     *       delayMs=0})
     *   <li>{@code ConnectionManager}에서 해당 bid를 강제 disconnect (진행 중인 backoff/reconnect 무시)
     *   <li>{@code connectToChat()}으로 완전히 새 연결 시작
     *   <li>첫 {@link ChatEvent#JOIN_CHANNEL} 수신 시 {@link ChatEvent#RECONNECTED} 이벤트 emit
     * </ol>
     *
     * @return 새 연결이 disconnect될 때 완료되는 future
     */
    public CompletableFuture<Void> forceReconnect() {
        eventEmitter.emit(
                ChatEvent.RECONNECTING,
                new ReconnectingEvent(
                        1, 1, 0L, ChatEvent.RECONNECTING, "", System.currentTimeMillis()));

        connectLock.lock();
        try {
            try {
                connectionManager.disconnect(config.getBid()).join();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error during force reconnect tear-down", e);
            }
            isConnected = false;
            connection = null;
            if (disconnectFuture != null && !disconnectFuture.isDone()) {
                disconnectFuture.complete(null);
            }
            eventEmitter.clearInternal();
        } finally {
            connectLock.unlock();
        }

        eventEmitter.once(
                ChatEvent.JOIN_CHANNEL,
                e ->
                        eventEmitter.emit(
                                ChatEvent.RECONNECTED,
                                new ReconnectedEvent(
                                        1, ChatEvent.RECONNECTED, "", System.currentTimeMillis())));

        return connectToChat();
    }

    public CompletableFuture<ConnectionStatus> getConnectionStatus() {
        SOOPConnection conn = this.connection;
        if (conn == null) {
            return CompletableFuture.completedFuture(new ConnectionStatus(false, false, 0));
        }

        return conn.getStatus();
    }

    public void disconnect() {
        connectLock.lock();
        try {
            if (!isConnected) {
                return;
            }

            try {
                connectionManager.disconnect(config.getBid()).join();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error during disconnect", e);
            } finally {
                isConnected = false;
                connection = null;
                if (disconnectFuture != null) {
                    disconnectFuture.complete(null);
                }
                eventEmitter.clearInternal();
            }
        } finally {
            connectLock.unlock();
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
}
