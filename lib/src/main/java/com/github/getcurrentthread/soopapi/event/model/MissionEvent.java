package com.github.getcurrentthread.soopapi.event.model;

import java.util.Map;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record MissionEvent(
        Map<String, Object> data, ChatEvent eventType, String raw, long timestamp)
        implements BaseEvent {}
