package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.code.UserLevel;
import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record JoinChannelEvent(
        String chatNo,
        String bjId,
        int maxSubBjCount,
        String familyNickname,
        String userFlag,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements SystemBaseEvent {

    /** {@code userFlag}("primary|secondary")를 {@link UserLevel}로 지연 파싱합니다. */
    public UserLevel userLevel() {
        return UserLevel.parse(userFlag);
    }
}
