package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record OGQEmoticonEvent(
    String chatNo, String message, String groupId, String subId,
    String version, String userInfo, String color, String chatLang, String type,
    ChatEvent eventType, String raw, long timestamp
) implements BaseEvent {}
