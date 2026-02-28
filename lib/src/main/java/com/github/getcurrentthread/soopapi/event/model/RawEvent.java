package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record RawEvent(ChatEvent eventType, String raw, long timestamp)
        implements SystemBaseEvent {}
