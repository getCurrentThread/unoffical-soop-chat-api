package com.github.getcurrentthread.soopapi.connection;

import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.getcurrentthread.soopapi.client.IChatMessageObserver;
import com.github.getcurrentthread.soopapi.config.SOOPChatConfig;
import com.github.getcurrentthread.soopapi.decoder.MessageDispatcher;
import com.github.getcurrentthread.soopapi.decoder.factory.DefaultMessageDecoderFactory;
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
    private volatile boolean isConnected;

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

    public CompletableFuture<Void> connect() {
        // 가상 스레드로 연결 로직 실행
        return CompletableFuture.runAsync(
                () -> {
                    try {
                        LOGGER.info("Fetching channel info for BID: " + config.getBid());
                        String bno =
                                config.getBno() != null
                                        ? config.getBno()
                                        : SOOPChatUtils.getBnoFromBid(config.getBid());

                        ChannelInfo channelInfo = SOOPChatUtils.getPlayerLive(bno, config.getBid());
                        LOGGER.info("Channel info received: " + channelInfo);

                        webSocketManager.connect(channelInfo).join();
                        isConnected = true;
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Connection failed", e);
                        throw new CompletionException(e);
                    }
                },
                Executors.newVirtualThreadPerTaskExecutor()); // 가상 스레드로 실행
    }

    private void notifyObservers(Message message) {
        if (message == null) {
            LOGGER.warning("Received null message");
            return;
        }

        LOGGER.info("Broadcasting message: " + message);

        // 각 옵저버 알림을 가상 스레드에서 처리
        for (IChatMessageObserver observer : observers) {
            CompletableFuture.runAsync(
                    () -> {
                        try {
                            observer.notify(message);
                        } catch (Exception e) {
                            LOGGER.log(Level.WARNING, "Error notifying observer", e);
                        }
                    },
                    messageProcessor);
        }
    }

    public void addObserver(IChatMessageObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
            LOGGER.info("Observer added, total observers: " + observers.size());
        }
    }

    public void removeObserver(IChatMessageObserver observer) {
        observers.remove(observer);
        LOGGER.info("Observer removed, remaining observers: " + observers.size());
    }

    public void disconnect() {
        try {
            webSocketManager.disconnect();
        } finally {
            isConnected = false;
            observers.clear();
        }
    }

    public boolean isConnected() {
        return isConnected && webSocketManager.isConnected();
    }
}
