package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.ArrayList;
import java.util.List;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.ChatUserEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class ChatUserDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 1;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        List<ChatUserEvent.ChatUserEntry> userList = new ArrayList<>();

        for (int i = 1; i < parts.length; i += 3) {
            if (i + 2 < parts.length) {
                userList.add(new ChatUserEvent.ChatUserEntry(parts[i], parts[i + 1], parts[i + 2]));
            }
        }

        return new ChatUserEvent(
                SOOPChatUtils.safeParseInt(parts[0], 0),
                userList,
                ChatEvent.CHAT_USER,
                raw,
                System.currentTimeMillis());
    }
}
