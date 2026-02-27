package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record ItemUsingEvent(
        String bjId, int itemType, int remainTime, ChatEvent eventType, String raw, long timestamp)
        implements BaseEvent {}
