package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record UnknownEvent(
    int value,
    ChatEvent eventType, String raw, long timestamp
) implements BaseEvent {}
