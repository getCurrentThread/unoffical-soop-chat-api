package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record VideoBalloonEvent(
    String chatNo, String bjId, String userId, String userNickname,
    int balloonCount, int fanOrder, int isTopFan, String relay, String fileName,
    boolean isDefault, String extraData,
    ChatEvent eventType, String raw, long timestamp
) implements BaseEvent {}
