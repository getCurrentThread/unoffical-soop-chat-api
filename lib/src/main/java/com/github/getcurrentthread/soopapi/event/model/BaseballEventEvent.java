package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record BaseballEventEvent(String eventData, ChatEvent eventType, String raw, long timestamp)
        implements ItemBaseEvent {}
