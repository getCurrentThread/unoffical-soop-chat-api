package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record TranslationEvent(
        int idx,
        int mode,
        String message,
        int orgLanguage,
        int transLanguage,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements BaseEvent {}
