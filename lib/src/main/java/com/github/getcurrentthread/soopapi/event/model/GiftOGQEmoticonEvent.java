package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record GiftOGQEmoticonEvent(
        String senderId,
        String senderNick,
        String receivedId,
        String receivedNick,
        String ogqTitle,
        String ogqImageUrl,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements BaseEvent {}
