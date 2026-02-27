package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record SetSubBjEvent(
        String userId,
        String flag,
        int hide,
        String nickname,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements BaseEvent {}
