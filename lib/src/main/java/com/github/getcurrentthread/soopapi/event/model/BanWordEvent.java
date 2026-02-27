package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record BanWordEvent(
        String replaceWord, String[] banWordList, ChatEvent eventType, String raw, long timestamp)
        implements ModerationBaseEvent {}
