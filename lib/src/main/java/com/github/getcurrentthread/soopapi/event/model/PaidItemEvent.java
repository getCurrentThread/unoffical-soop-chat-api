package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record PaidItemEvent(
    int itemType, String bjId, String buyerId, String buyerNickname,
    String itemName, int itemCount,
    ChatEvent eventType, String raw, long timestamp
) implements BaseEvent {}
