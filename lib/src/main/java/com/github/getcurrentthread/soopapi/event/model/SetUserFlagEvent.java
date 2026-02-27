package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record SetUserFlagEvent(
        String oldFlag,
        String userId,
        String userNickname,
        String newFlag,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements SystemBaseEvent {}
