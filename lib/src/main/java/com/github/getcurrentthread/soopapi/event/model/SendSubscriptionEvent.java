package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record SendSubscriptionEvent(
    String senderId, String senderNickname, String receiverId,
    String receiverNickname, String subscriptionId, String subscriptionNickname,
    int itemType, String itemCode, int isSubscription, String subscriptionType,
    String subscriptionPeriod, int subscriptionRemain, int subscriptionPaycount,
    ChatEvent eventType, String raw, long timestamp
) implements BaseEvent {}
