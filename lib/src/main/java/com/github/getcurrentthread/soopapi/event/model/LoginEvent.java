package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record LoginEvent(
        String userId, String userFlag, ChatEvent eventType, String raw, long timestamp)
        implements BaseEvent {}
