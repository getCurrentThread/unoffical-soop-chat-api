package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record GiftTicketEvent(
        String senderId,
        String senderNickname,
        String receiverId,
        String receiverNickname,
        String ticketData,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements DonationBaseEvent {}
