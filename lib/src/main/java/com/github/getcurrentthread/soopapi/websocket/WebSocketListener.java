package com.github.getcurrentthread.soopapi.websocket;

import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.getcurrentthread.soopapi.decoder.MessageDispatcher;
import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.EventEmitter;
import com.github.getcurrentthread.soopapi.event.model.DisconnectedEvent;

public class WebSocketListener implements WebSocket.Listener {
    private static final Logger LOGGER = Logger.getLogger(WebSocketListener.class.getName());
    static final int DEFAULT_BUFFER_SIZE = 16384;
    private final MessageDispatcher messageDispatcher;
    private final EventEmitter eventEmitter;
    private final StringBuilder textBuffer = new StringBuilder(DEFAULT_BUFFER_SIZE);

    private final java.io.ByteArrayOutputStream binaryBuffer =
            new java.io.ByteArrayOutputStream(DEFAULT_BUFFER_SIZE);

    public WebSocketListener(MessageDispatcher messageDispatcher, EventEmitter eventEmitter) {
        this.messageDispatcher = Objects.requireNonNull(messageDispatcher, "messageDispatcher");
        this.eventEmitter = Objects.requireNonNull(eventEmitter, "eventEmitter");
        LOGGER.fine(() -> "WebSocketListener initialized with dispatcher: " + messageDispatcher);
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        LOGGER.info("WebSocket connection opened");
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        try {
            textBuffer.append(data);

            if (last) {
                String message = textBuffer.toString();
                messageDispatcher.dispatchMessage(message);

                textBuffer.setLength(0);
                if (textBuffer.capacity() > DEFAULT_BUFFER_SIZE * 4) {
                    textBuffer.trimToSize();
                    textBuffer.ensureCapacity(DEFAULT_BUFFER_SIZE);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing text message", e);
        }

        webSocket.request(1);
        return null;
    }

    @Override
    public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
        try {
            byte[] bytes = new byte[data.remaining()];
            data.get(bytes);
            binaryBuffer.write(bytes);

            if (last) {
                String message = binaryBuffer.toString(java.nio.charset.StandardCharsets.UTF_8);
                messageDispatcher.dispatchMessage(message);

                binaryBuffer.reset();
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
        String errorMessage =
                error.getMessage() != null ? error.getMessage() : error.getClass().getName();
        eventEmitter.emit(
                ChatEvent.DISCONNECTED,
                new DisconnectedEvent(
                        -1,
                        errorMessage,
                        true,
                        ChatEvent.DISCONNECTED,
                        "",
                        System.currentTimeMillis()));
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        LOGGER.info("WebSocket closed: statusCode=" + statusCode + ", reason=" + reason);
        eventEmitter.emit(
                ChatEvent.DISCONNECTED,
                new DisconnectedEvent(
                        statusCode,
                        reason,
                        false,
                        ChatEvent.DISCONNECTED,
                        "",
                        System.currentTimeMillis()));
        return null;
    }
}
