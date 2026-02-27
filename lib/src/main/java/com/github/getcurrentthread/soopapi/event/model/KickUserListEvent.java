package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

import java.util.List;

public record KickUserListEvent(
    List<KickedUser> kickedUsers,
    ChatEvent eventType, String raw, long timestamp
) implements BaseEvent {
    public record KickedUser(String userId, String userNickname, String time,
        String orderUserId, String orderUserNickname, String orderUserFlag) {}
}
