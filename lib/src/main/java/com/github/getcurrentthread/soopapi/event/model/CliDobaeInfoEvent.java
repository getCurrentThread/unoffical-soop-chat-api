package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record CliDobaeInfoEvent(
        int dobaeInfo, String userId, ChatEvent eventType, String raw, long timestamp)
        implements ModerationBaseEvent {}
