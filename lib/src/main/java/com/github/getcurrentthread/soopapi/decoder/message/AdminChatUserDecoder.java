package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.ArrayList;
import java.util.List;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.AdminChatUserEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;

public class AdminChatUserDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        String type = "1".equals(parts[0]) ? "list" : parts[0];
        List<AdminChatUserEvent.AdminChatUserEntry> users = new ArrayList<>();
        if ("1".equals(parts[0])) {
            for (int i = 1; i < parts.length && parts[i] != null && !parts[i].isEmpty(); i += 3) {
                if (i + 2 < parts.length) {
                    users.add(
                            new AdminChatUserEvent.AdminChatUserEntry(
                                    parts[i], parts[i + 1], parts[i + 2]));
                }
            }
        }
        return new AdminChatUserEvent(
                type, users, ChatEvent.ADMIN_CHAT_USER, raw, System.currentTimeMillis());
    }
}
