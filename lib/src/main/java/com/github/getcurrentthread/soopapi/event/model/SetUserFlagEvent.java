package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.code.UserLevel;
import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record SetUserFlagEvent(
        String oldFlag,
        String userId,
        String userNickname,
        String newFlag,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements SystemBaseEvent {

    /** {@code oldFlag}("primary|secondary")를 {@link UserLevel}로 지연 파싱합니다. */
    public UserLevel oldLevel() {
        return UserLevel.parse(oldFlag);
    }

    /** {@code newFlag}("primary|secondary")를 {@link UserLevel}로 지연 파싱합니다. */
    public UserLevel newLevel() {
        return UserLevel.parse(newFlag);
    }
}
