package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.ArrayList;
import java.util.List;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.ChatUserEvent;

public class ChatUserDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        List<ChatUserEvent.ChatUserEntry> userList = new ArrayList<>();

        for (int i = 1; i < parts.length; i += 3) {
            if (i + 2 < parts.length) {
                userList.add(new ChatUserEvent.ChatUserEntry(parts[i], parts[i + 1], parts[i + 2]));
            }
        }

        return new ChatUserEvent(
                Integer.valueOf(parts[0]),
                userList,
                ChatEvent.CHAT_USER,
                raw,
                System.currentTimeMillis());
    }
}
