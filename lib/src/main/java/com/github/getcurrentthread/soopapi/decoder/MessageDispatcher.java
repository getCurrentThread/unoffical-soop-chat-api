package com.github.getcurrentthread.soopapi.decoder;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.getcurrentthread.soopapi.constant.SOOPConstants;
import com.github.getcurrentthread.soopapi.decoder.message.IMessageDecoder;
import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.EventEmitter;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.RawEvent;
import com.github.getcurrentthread.soopapi.event.model.UnknownEvent;

public class MessageDispatcher {
    private static final Logger LOGGER = Logger.getLogger(MessageDispatcher.class.getName());

    private final Map<ChatEvent, IMessageDecoder> messageDecoders;
    private final ExecutorService messageProcessor;
    private final EventEmitter eventEmitter;

    public MessageDispatcher(
            Map<ChatEvent, IMessageDecoder> messageDecoders,
            ExecutorService messageProcessor,
            EventEmitter eventEmitter) {
        this.messageDecoders = messageDecoders;
        this.messageProcessor = messageProcessor;
        this.eventEmitter = eventEmitter;
    }

    public void dispatchMessage(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }

        messageProcessor.execute(
                () -> {
                    try {
                        if (eventEmitter.hasListeners(ChatEvent.RAW)) {
                            eventEmitter.emit(
                                    ChatEvent.RAW,
                                    new RawEvent(
                                            ChatEvent.RAW, message, System.currentTimeMillis()));
                        }

                        int firstSep = message.indexOf(SOOPConstants.F_CHAR);
                        if (firstSep < 0) {
                            return;
                        }

                        String header = message.substring(0, firstSep);
                        int serviceCode = parseServiceCode(header);
                        ChatEvent chatEvent = ChatEvent.fromCode(serviceCode);

                        if (!eventEmitter.hasListeners(chatEvent)) {
                            return;
                        }

                        IMessageDecoder decoder = messageDecoders.get(chatEvent);

                        String[] messageParts =
                                message.substring(firstSep + 1).split(SOOPConstants.F);

                        BaseEvent event;
                        if (decoder != null) {
                            event = decoder.decode(messageParts, message);
                        } else {
                            event =
                                    new UnknownEvent(
                                            serviceCode,
                                            message,
                                            ChatEvent.NONE_TYPE,
                                            message,
                                            System.currentTimeMillis());
                        }

                        if (event != null) {
                            eventEmitter.emit(chatEvent, event);
                        }
                    } catch (Exception e) {
                        if (LOGGER.isLoggable(Level.WARNING)) {
                            String truncated =
                                    message.length() > 200
                                            ? message.substring(0, 200) + "..."
                                            : message;
                            LOGGER.log(Level.WARNING, "Error processing message: " + truncated, e);
                        }
                    }
                });
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
