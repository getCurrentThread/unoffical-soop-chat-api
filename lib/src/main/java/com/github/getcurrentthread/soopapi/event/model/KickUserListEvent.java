package com.github.getcurrentthread.soopapi.event.model;

import java.util.List;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record KickUserListEvent(
        List<KickedUser> kickedUsers, ChatEvent eventType, String raw, long timestamp)
        implements ModerationBaseEvent {
    public record KickedUser(
            String userId,
            String userNickname,
            String time,
            String orderUserId,
            String orderUserNickname,
            String orderUserFlag) {}
}
