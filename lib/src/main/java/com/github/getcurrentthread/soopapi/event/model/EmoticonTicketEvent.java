package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record EmoticonTicketEvent(int value, ChatEvent eventType, String raw, long timestamp)
        implements SystemBaseEvent {}
