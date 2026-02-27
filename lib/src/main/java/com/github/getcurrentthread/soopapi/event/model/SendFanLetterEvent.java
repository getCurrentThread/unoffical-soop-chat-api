package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record SendFanLetterEvent(
        String bjId,
        String bjNickname,
        String senderId,
        String senderNickname,
        int type,
        int count,
        String supporterOrder,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements DonationBaseEvent {}
