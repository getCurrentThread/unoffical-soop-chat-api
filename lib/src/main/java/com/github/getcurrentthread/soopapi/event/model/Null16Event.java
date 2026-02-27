package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record Null16Event(
    ChatEvent eventType, String raw, long timestamp
) implements BaseEvent {}
