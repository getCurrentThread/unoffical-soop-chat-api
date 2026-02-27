package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record NoticeEvent(
    String message,
    ChatEvent eventType, String raw, long timestamp
) implements BaseEvent {}
