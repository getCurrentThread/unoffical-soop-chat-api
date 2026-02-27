package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record SendBalloonSubEvent(
        String bjId,
        String senderId,
        String senderNickname,
        int count,
        int fanOrder,
        String fileName,
        boolean isDefault,
        int isTopFan,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements BaseEvent {}
