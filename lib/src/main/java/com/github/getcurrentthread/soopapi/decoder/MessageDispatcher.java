package com.github.getcurrentthread.soopapi.decoder;

import com.github.getcurrentthread.soopapi.constant.SOOPConstants;
import com.github.getcurrentthread.soopapi.decoder.message.IMessageDecoder;
import com.github.getcurrentthread.soopapi.model.Message;
import com.github.getcurrentthread.soopapi.model.MessageType;

import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.github.getcurrentthread.soopapi.util.SOOPChatUtils.parseServiceCode;

public class MessageDispatcher {
    private static final Logger LOGGER = Logger.getLogger(MessageDispatcher.class.getName());

    private final Map<MessageType, IMessageDecoder> messageDecoders;
    private Consumer<Message> messageHandler;

    public MessageDispatcher(Map<MessageType, IMessageDecoder> messageDecoders) {
        this.messageDecoders = messageDecoders;
    }

    public void setMessageHandler(Consumer<Message> messageHandler) {
        this.messageHandler = messageHandler;
    }

    public void dispatchMessage(String message) {
        try {
            LOGGER.finest("Dispatching message: " + message);
            Message decodedMessage = decodeMessage(message);
            if (decodedMessage != null) {
                if (messageHandler != null) {
                    messageHandler.accept(decodedMessage);
                }
            }
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "Error dispatching message: " + message, e);
        }
    }

    private Message decodeMessage(String message) {
        String[] parts = message.split(SOOPConstants.F);
        if (parts.length < 2) {
            return null;
        }

        int serviceCode = parseServiceCode(parts[0]);
        MessageType messageType = MessageType.fromCode(serviceCode);

        IMessageDecoder decoder = messageDecoders.getOrDefault(messageType, _parts -> null);

        String[] messageParts = new String[parts.length - 1];
        System.arraycopy(parts, 1, messageParts, 0, parts.length - 1);

        try {
            Map<String, Object> decodedData = decoder.decode(messageParts);
            return new Message(messageType, decodedData, message);
        } catch (RuntimeException e) {
            return new Message(messageType, null, message);
        }
    }
}