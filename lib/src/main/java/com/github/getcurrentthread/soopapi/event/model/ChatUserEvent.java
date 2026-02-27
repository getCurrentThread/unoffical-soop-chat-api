package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

import java.util.List;

public record ChatUserEvent(
    int type, List<ChatUserEntry> userList,
    ChatEvent eventType, String raw, long timestamp
) implements BaseEvent {
    public record ChatUserEntry(String id, String nickname, String flag) {}
}
