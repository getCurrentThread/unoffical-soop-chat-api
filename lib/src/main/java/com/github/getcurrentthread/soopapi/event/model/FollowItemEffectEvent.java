package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record FollowItemEffectEvent(
        String bjId,
        String sendId,
        String sendNick,
        int month,
        int chatNo,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements BaseEvent {}
