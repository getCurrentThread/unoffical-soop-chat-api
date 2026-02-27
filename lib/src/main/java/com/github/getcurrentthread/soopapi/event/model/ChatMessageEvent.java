package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record ChatMessageEvent(
        String message,
        String senderId,
        int type,
        int chatLang,
        String senderNickname,
        String senderFlag,
        String subscriptionMonth,
        String randomNicknameColor,
        String randomNicknameColorDarkmode,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements BaseEvent {}
