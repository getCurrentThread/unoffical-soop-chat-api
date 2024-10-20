package com.github.getcurrentthread.soopapi.websocket;

import com.github.getcurrentthread.soopapi.decoder.MessageDispatcher;

import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebSocketListener implements WebSocket.Listener {
    private static final Logger LOGGER = Logger.getLogger(WebSocketListener.class.getName());

    private final MessageDispatcher messageDispatcher;
    private ByteBuffer buffer = ByteBuffer.allocate(16384);

    public WebSocketListener(MessageDispatcher messageDispatcher) {
        this.messageDispatcher = messageDispatcher;
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        LOGGER.info("WebSocket connection opened");
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        byte[] bytes = data.toString().getBytes(StandardCharsets.UTF_8);
        return onBinary(webSocket, ByteBuffer.wrap(bytes), last);
    }

    @Override
    public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
        if (buffer.remaining() < data.remaining()) {
            ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() + data.remaining());
            buffer.flip();
            newBuffer.put(buffer);
            buffer = newBuffer;
        }
        buffer.put(data);

        if (last) {
            buffer.flip();
            String message = StandardCharsets.UTF_8.decode(buffer).toString();
            messageDispatcher.dispatchMessage(message);
            buffer = ByteBuffer.allocate(16384);
        }

        webSocket.request(1);
        return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        LOGGER.log(Level.SEVERE, "WebSocket error occurred", error);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        LOGGER.log(Level.INFO, "WebSocket connection closed: {0}", reason);
        return null;
    }
}