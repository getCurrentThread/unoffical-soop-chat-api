package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public sealed interface BaseEvent
        permits ChatBaseEvent,
                DonationBaseEvent,
                SystemBaseEvent,
                ModerationBaseEvent,
                ItemBaseEvent,
                NotificationBaseEvent,
                UnknownEvent {
    ChatEvent eventType();

    String raw();

    long timestamp();
}
