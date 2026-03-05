package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record DisconnectedEvent(
        int statusCode,
        String reason,
        boolean causedByError,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements SystemBaseEvent {}
