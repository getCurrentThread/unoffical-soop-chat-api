package com.github.getcurrentthread.soopapi.decoder;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.getcurrentthread.soopapi.constant.SOOPConstants;
import com.github.getcurrentthread.soopapi.decoder.message.IMessageDecoder;
import com.github.getcurrentthread.soopapi.model.Message;
import com.github.getcurrentthread.soopapi.model.MessageType;

public class MessageDispatcher {
    private static final Logger LOGGER = Logger.getLogger(MessageDispatcher.class.getName());

    private final Map<MessageType, IMessageDecoder> messageDecoders;
    private final Executor messageProcessor;
    private Consumer<Message> messageHandler;

    public MessageDispatcher(
            Map<MessageType, IMessageDecoder> messageDecoders, Executor messageProcessor) {
        this.messageDecoders = messageDecoders;
        this.messageProcessor = messageProcessor;
    }

    public void setMessageHandler(Consumer<Message> messageHandler) {
        this.messageHandler = messageHandler;
    }

    public void dispatchMessage(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }

        // 가상 스레드에서 메시지 처리
        messageProcessor.execute(
                () -> {
                    try {
                        Message decodedMessage = decodeMessage(message);
                        if (decodedMessage != null && messageHandler != null) {
                            messageHandler.accept(decodedMessage);
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Error processing message: " + message, e);
                    }
                });
    }

    private Message decodeMessage(String message) {
        String[] parts = message.split(SOOPConstants.F);
        if (parts.length < 2) {
            return null;
        }

        try {
            int serviceCode = parseServiceCode(parts[0]);
            MessageType messageType = MessageType.fromCode(serviceCode);
            IMessageDecoder decoder = messageDecoders.get(messageType);

            if (decoder == null) {
                return new Message(messageType, null, message);
            }

            String[] messageParts = new String[parts.length - 1];
            System.arraycopy(parts, 1, messageParts, 0, parts.length - 1);

            Map<String, Object> decodedData = decoder.decode(messageParts);
            return new Message(messageType, decodedData, message);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error decoding message", e);
            return null;
        }
    }

    private int parseServiceCode(String header) {
        try {
            String[] headerParts = header.split("\t");
            if (headerParts.length < 2) {
                return -1;
            }
            String lastPart = headerParts[headerParts.length - 1];
            if (lastPart.length() < 4) {
                return -1;
            }
            return Integer.parseInt(lastPart.substring(0, 4));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error parsing service code", e);
            return -1;
        }
    }
}
