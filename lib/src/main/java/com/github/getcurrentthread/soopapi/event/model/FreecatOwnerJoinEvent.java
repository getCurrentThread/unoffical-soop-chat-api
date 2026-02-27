package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record FreecatOwnerJoinEvent(
        String chatNo,
        String bjId,
        int maxSubBjCount,
        String familyNickname,
        String userFlag,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements BaseEvent {}
