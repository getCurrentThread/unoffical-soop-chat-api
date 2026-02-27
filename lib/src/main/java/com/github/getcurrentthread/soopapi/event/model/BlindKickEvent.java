package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record BlindKickEvent(
        String userId,
        String userNickname,
        int blindTime,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements BaseEvent {}
