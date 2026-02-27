package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record KickAndCancelEvent(
        int status,
        String userId,
        String userNickname,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements ModerationBaseEvent {}
