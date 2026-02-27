package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record SendPromotionEvent(ChatEvent eventType, String raw, long timestamp)
        implements DonationBaseEvent {}
