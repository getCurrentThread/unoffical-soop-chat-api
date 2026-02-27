package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record NotiGameRankerEvent(ChatEvent eventType, String raw, long timestamp)
        implements SystemBaseEvent {}
