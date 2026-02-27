package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record SnsMessageEvent(
    String message,
    ChatEvent eventType, String raw, long timestamp
) implements BaseEvent {}
