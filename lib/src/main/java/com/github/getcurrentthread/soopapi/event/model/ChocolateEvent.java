package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record ChocolateEvent(
        String bjId,
        String senderId,
        String senderNickname,
        int count,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements BaseEvent {}
