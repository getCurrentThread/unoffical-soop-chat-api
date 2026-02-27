package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record NotifyPollEvent(
    int status, String bjId, int no, int show,
    ChatEvent eventType, String raw, long timestamp
) implements BaseEvent {}
