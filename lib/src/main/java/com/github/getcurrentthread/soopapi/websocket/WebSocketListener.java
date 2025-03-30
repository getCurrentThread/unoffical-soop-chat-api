package com.github.getcurrentthread.soopapi.websocket;

import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.getcurrentthread.soopapi.constant.SOOPConstants;
import com.github.getcurrentthread.soopapi.decoder.MessageDispatcher;

public class WebSocketListener implements WebSocket.Listener {
    private static final Logger LOGGER = Logger.getLogger(WebSocketListener.class.getName());
    private final MessageDispatcher messageDispatcher;
    private ByteBuffer buffer = ByteBuffer.allocate(16384);
    private static final boolean DEBUG = true;

    public WebSocketListener(MessageDispatcher messageDispatcher) {
        this.messageDispatcher = messageDispatcher;
        LOGGER.info("WebSocketListener initialized with dispatcher: " + messageDispatcher);
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        LOGGER.info("WebSocket connection opened");
        debugLog("WebSocket opened with protocol: " + webSocket.getSubprotocol());
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        debugLog("Received text message of length " + data.length() + ", last: " + last);
        debugLog("Text content: " + data.toString());

        byte[] bytes = data.toString().getBytes(StandardCharsets.UTF_8);
        return onBinary(webSocket, ByteBuffer.wrap(bytes), last);
    }

    @Override
    public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
        try {
            debugLog("Binary message received, size: " + data.remaining() + ", last: " + last);

            if (buffer.remaining() < data.remaining()) {
                int newSize = buffer.capacity() + data.remaining();
                debugLog("Buffer expansion needed. New size: " + newSize);
                ByteBuffer newBuffer = ByteBuffer.allocate(newSize);
                buffer.flip();
                newBuffer.put(buffer);
                buffer = newBuffer;
            }

            buffer.put(data);

            if (last) {
                buffer.flip();
                String message = StandardCharsets.UTF_8.decode(buffer).toString();
                debugLog(
                        "Complete message ready for processing: "
                                + message.replace(SOOPConstants.ESC, "\\ESC")
                                        .replace(SOOPConstants.F, "\\F"));

                if (messageDispatcher == null) {
                    LOGGER.severe("MessageDispatcher is null!");
                } else {
                    messageDispatcher.dispatchMessage(message);
                }

                buffer = ByteBuffer.allocate(16384);
            } else {
                debugLog("Partial message received, waiting for more data");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing binary message", e);
            e.printStackTrace();
        }

        webSocket.request(1);
        return null;
    }

    @Override
    public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
        debugLog("Ping received");
        webSocket.request(1);
        return null;
    }

    @Override
    public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
        debugLog("Pong received");
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
        debugLog("WebSocket closed with status: " + statusCode + ", reason: " + reason);
        return null;
    }

    private void debugLog(String message) {
        if (DEBUG) {
            LOGGER.info("[DEBUG] " + message);
        }
    }
}
