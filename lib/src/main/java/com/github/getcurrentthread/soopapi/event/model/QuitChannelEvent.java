package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.code.ChatQuitStatus;
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
        implements SystemBaseEvent {

    /** {@code quitType}을 {@link ChatQuitStatus}로 지연 변환합니다. */
    public ChatQuitStatus quitStatus() {
        return ChatQuitStatus.fromCode(quitType);
    }
}
