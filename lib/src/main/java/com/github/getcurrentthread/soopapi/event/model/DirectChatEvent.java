package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record DirectChatEvent(
        String message,
        String senderId,
        String receiverId,
        int type,
        String senderNickname,
        String receiverNickname,
        String flag,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements ChatBaseEvent {}
