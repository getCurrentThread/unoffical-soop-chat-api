package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

import java.util.List;

public record AdminChatUserEvent(
    String type, List<AdminChatUserEntry> users,
    ChatEvent eventType, String raw, long timestamp
) implements BaseEvent {
    public record AdminChatUserEntry(String id, String nickname, String flag) {}
}
