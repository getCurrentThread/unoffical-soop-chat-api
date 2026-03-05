package com.github.getcurrentthread.soopapi.exception;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public class EventEmitterException extends SOOPChatException {
    private final ChatEvent chatEvent;

    public EventEmitterException(String message, Throwable cause, ChatEvent chatEvent) {
        super(message, cause);
        this.chatEvent = chatEvent;
    }

    public ChatEvent getChatEvent() {
        return chatEvent;
    }
}
