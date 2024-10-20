package com.github.getcurrentthread.soopapi.websocket;

import com.github.getcurrentthread.soopapi.config.SOOPChatConfig;
import com.github.getcurrentthread.soopapi.constant.SOOPConstants;
import com.github.getcurrentthread.soopapi.exception.SOOPChatException;
import com.github.getcurrentthread.soopapi.model.ChannelInfo;
import com.github.getcurrentthread.soopapi.util.SSLContextProvider;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebSocketManager {
    private static final Logger LOGGER = Logger.getLogger(WebSocketManager.class.getName());

    private final SOOPChatConfig config;
    private final SSLContext sslContext;
    private final ScheduledExecutorService scheduler;
    private final WebSocketListener listener;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private WebSocket webSocket;

    public WebSocketManager(SOOPChatConfig config, SSLContext sslContext, ScheduledExecutorService scheduler, WebSocketListener listener) {
        this.config = config;
        this.sslContext = sslContext != null ? sslContext : SSLContextProvider.getInstance();
        this.scheduler = scheduler;
        this.listener = listener;
    }

    public CompletableFuture<Void> connect(ChannelInfo channelInfo) {
        String wsUrl = String.format("wss://%s:%s/Websocket/%s", channelInfo.CHDOMAIN, channelInfo.CHPT, config.getBid());
        return HttpClient.newBuilder()
                .sslContext(sslContext)
                .build()
                .newWebSocketBuilder()
                .subprotocols("chat")
                .buildAsync(URI.create(wsUrl), listener)
                .thenAccept(ws -> {
                    this.webSocket = ws;
                    isConnected.set(true);
                    sendInitialPackets(channelInfo);
                    startPingScheduler();
                });
    }

    private void sendInitialPackets(ChannelInfo channelInfo) {
        String connectPacket = SOOPConstants.ESC + "000100000600" + SOOPConstants.F.repeat(3) + "16" + SOOPConstants.F;
        String joinPacket = SOOPConstants.ESC + "0002" + String.format("%06d", calculateByteSize(channelInfo.CHATNO)) + "00" + SOOPConstants.F + channelInfo.CHATNO + SOOPConstants.F.repeat(5);

        webSocket.sendText(connectPacket, true)
                .thenRun(() -> {
                    try {
                        Thread.sleep(3000);
                        webSocket.sendText(joinPacket, true);
                    } catch (InterruptedException e) {
                        LOGGER.log(Level.WARNING, "Interrupted while sending initial packets", e);
                        Thread.currentThread().interrupt();
                    }
                });
    }

    private void startPingScheduler() {
        String pingPacket = SOOPConstants.ESC + "000000000100" + SOOPConstants.F;
        scheduler.scheduleAtFixedRate(() -> {
            if (webSocket != null) {
                webSocket.sendText(pingPacket, true);
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

    public void sendMessage(String message) {
        if (webSocket != null && isConnected.get()) {
            webSocket.sendText(message, true);
        } else {
            throw new SOOPChatException("WebSocket is not connected");
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Disconnecting");
            isConnected.set(false);
        }
    }

    public boolean isConnected() {
        return isConnected.get();
    }

    private int calculateByteSize(String string) {
        return string.getBytes().length + 6;
    }
}