package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

import java.util.Map;

public record ChuserExtendEvent(
    Map<String, Map<String, Integer>> userStatus,
    ChatEvent eventType, String raw, long timestamp
) implements BaseEvent {}
