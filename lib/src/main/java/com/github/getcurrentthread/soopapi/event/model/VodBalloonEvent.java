package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record VodBalloonEvent(
        String bjId,
        String senderId,
        String senderNickname,
        int balloonCount,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements DonationBaseEvent {}
