package com.github.getcurrentthread.soopapi.websocket;

import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.getcurrentthread.soopapi.decoder.MessageDispatcher;

public class WebSocketListener implements WebSocket.Listener {
    private static final Logger LOGGER = Logger.getLogger(WebSocketListener.class.getName());
    private static final int DEFAULT_BUFFER_SIZE = 16384;
    private final MessageDispatcher messageDispatcher;
    private ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);

    public WebSocketListener(MessageDispatcher messageDispatcher) {
        this.messageDispatcher = messageDispatcher;
        LOGGER.info("WebSocketListener initialized with dispatcher: " + messageDispatcher);
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        LOGGER.info("WebSocket connection opened");
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        ByteBuffer encoded = StandardCharsets.UTF_8.encode(CharBuffer.wrap(data));
        return onBinary(webSocket, encoded, last);
    }

    @Override
    public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
        try {
            if (buffer.remaining() < data.remaining()) {
                int newSize = buffer.position() + data.remaining();
                ByteBuffer newBuffer = ByteBuffer.allocate(newSize);
                buffer.flip();
                newBuffer.put(buffer);
                buffer = newBuffer;
            }

            buffer.put(data);

            if (last) {
                buffer.flip();
                String message = StandardCharsets.UTF_8.decode(buffer).toString();

                if (messageDispatcher == null) {
                    LOGGER.severe("MessageDispatcher is null!");
                } else {
                    messageDispatcher.dispatchMessage(message);
                }

                buffer.clear();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing binary message", e);
        }

        webSocket.request(1);
        return null;
    }

    @Override
    public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
        webSocket.request(1);
        return null;
    }

    @Override
    public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
        webSocket.request(1);
        return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        LOGGER.log(Level.SEVERE, "WebSocket error", error);
        error.printStackTrace();
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        return null;
    }
}
