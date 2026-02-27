package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record ItemDropsEvent(
    String bjId, String dropsName, String dropsMsg, String dropsImgUrl,
    ChatEvent eventType, String raw, long timestamp
) implements BaseEvent {}
