package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record SendBalloonEvent(
        String bjId,
        String senderId,
        String senderNickname,
        int count,
        int fanOrder,
        String fileName,
        boolean isDefault,
        int isTopFan,
        String ttsData,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements BaseEvent {}
