package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record ManagerChatEvent(
    String message, String senderId, int isAdmin, int chatLang,
    String senderNickname, String senderFlag, String subscriptionMonth,
    ChatEvent eventType, String raw, long timestamp
) implements BaseEvent {}
