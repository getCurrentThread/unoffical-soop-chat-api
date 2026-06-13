package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.code.UserLevel;
import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record SetAdminFlagEvent(String flag, ChatEvent eventType, String raw, long timestamp)
        implements ModerationBaseEvent {

    /** {@code flag}("primary|secondary")를 {@link UserLevel}로 지연 파싱합니다. */
    public UserLevel level() {
        return UserLevel.parse(flag);
    }
}
