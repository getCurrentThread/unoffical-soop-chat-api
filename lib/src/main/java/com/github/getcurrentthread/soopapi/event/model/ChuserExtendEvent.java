package com.github.getcurrentthread.soopapi.event.model;

import java.util.Map;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record ChuserExtendEvent(
        Map<String, Map<String, Integer>> userStatus,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements SystemBaseEvent {}
