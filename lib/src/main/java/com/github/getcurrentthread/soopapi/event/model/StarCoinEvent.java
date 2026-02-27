package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record StarCoinEvent(
    ChatEvent eventType, String raw, long timestamp
) implements BaseEvent {}
