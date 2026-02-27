package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record SnsModeEvent(int snsMode, ChatEvent eventType, String raw, long timestamp)
        implements SystemBaseEvent {}
