package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record AdconEffectEvent(
    int chatNo, String bjId, String senderId, String senderNickname,
    String message, String message2, String title, String urlImg, String urlDefault,
    int adconCount, int fanOrder, int isTopFan, int isFanChief, int isSubRoom,
    ChatEvent eventType, String raw, long timestamp
) implements BaseEvent {}
