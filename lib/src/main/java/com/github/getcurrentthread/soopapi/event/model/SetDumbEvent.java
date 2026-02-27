package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record SetDumbEvent(
        String userId,
        String userInfo,
        int dumbTime,
        int dumbCount,
        String adminId,
        int adminType,
        String extraInfo,
        String userNickname,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements ModerationBaseEvent {}
