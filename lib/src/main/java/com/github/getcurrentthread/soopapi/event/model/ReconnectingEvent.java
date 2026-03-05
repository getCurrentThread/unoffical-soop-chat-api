package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record ReconnectingEvent(
        int attemptNumber,
        int maxAttempts,
        long delayMs,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements SystemBaseEvent {}
