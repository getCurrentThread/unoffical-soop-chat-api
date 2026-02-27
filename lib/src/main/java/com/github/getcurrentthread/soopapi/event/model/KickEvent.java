package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record KickEvent(
        String userId,
        String userNickname,
        int kickType,
        String reason,
        String kickerNickname,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements BaseEvent {}
