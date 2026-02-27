package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record TranslationStateEvent(
    int state,
    ChatEvent eventType, String raw, long timestamp
) implements BaseEvent {}
