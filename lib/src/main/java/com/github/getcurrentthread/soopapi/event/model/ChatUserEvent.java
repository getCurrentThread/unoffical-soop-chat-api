package com.github.getcurrentthread.soopapi.event.model;

import java.util.List;

import com.github.getcurrentthread.soopapi.code.UserLevel;
import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record ChatUserEvent(
        int type, List<ChatUserEntry> userList, ChatEvent eventType, String raw, long timestamp)
        implements SystemBaseEvent {
    public record ChatUserEntry(String id, String nickname, String flag) {

        /** {@code flag}("primary|secondary")를 {@link UserLevel}로 지연 파싱합니다. */
        public UserLevel level() {
            return UserLevel.parse(flag);
        }
    }
}
