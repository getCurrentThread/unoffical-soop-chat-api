package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record IceModeExEvent(
    int iceMode, int freezeType, int balloonLimitCount, int subscriptionLimitCount,
    ChatEvent eventType, String raw, long timestamp
) implements BaseEvent {}
