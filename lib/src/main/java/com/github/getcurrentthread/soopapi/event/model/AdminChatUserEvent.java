package com.github.getcurrentthread.soopapi.event.model;

import java.util.List;

import com.github.getcurrentthread.soopapi.code.UserLevel;
import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record AdminChatUserEvent(
        String type,
        List<AdminChatUserEntry> users,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements ModerationBaseEvent {
    public record AdminChatUserEntry(String id, String nickname, String flag) {

        /** {@code flag}("primary|secondary")를 {@link UserLevel}로 지연 파싱합니다. */
        public UserLevel level() {
            return UserLevel.parse(flag);
        }
    }
}
