package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record QuickViewEvent(
        String senderId,
        String senderNickname,
        String receiverId,
        String receiverNickname,
        int itemType,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements ItemBaseEvent {}
