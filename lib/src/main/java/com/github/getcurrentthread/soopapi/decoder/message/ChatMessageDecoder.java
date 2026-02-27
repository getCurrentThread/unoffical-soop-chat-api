package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.ChatMessageEvent;

public class ChatMessageDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new ChatMessageEvent(
                parts[0],
                parts[1],
                Integer.parseInt(parts[3]),
                Integer.parseInt(parts[4]),
                parts[5],
                parts[6],
                parts[7],
                parts.length > 8 ? parts[8] : "",
                parts.length > 9 ? parts[9] : "",
                ChatEvent.CHAT_MESSAGE,
                raw,
                System.currentTimeMillis());
    }
}
