package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record SetBroadInfoEvent(ChatEvent eventType, String raw, long timestamp)
        implements SystemBaseEvent {}
