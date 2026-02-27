package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record QuitChannelEvent(
        int quitType,
        int adminKickCount,
        String nickname,
        String bannedRoomBjId,
        String bannedRoomBjNickname,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements SystemBaseEvent {}
