package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record SetNicknameEvent(
    String userId, String newNickname, int changeType,
    String flag, String oldNickname,
    ChatEvent eventType, String raw, long timestamp
) implements BaseEvent {}
