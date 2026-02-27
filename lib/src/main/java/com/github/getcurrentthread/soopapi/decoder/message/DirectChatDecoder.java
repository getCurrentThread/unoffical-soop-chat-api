package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.DirectChatEvent;

public class DirectChatDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new DirectChatEvent(
                parts[0],
                parts[1],
                parts[2],
                Integer.parseInt(parts[3]),
                parts[5],
                parts[6],
                parts[7],
                ChatEvent.DIRECT_CHAT,
                raw,
                System.currentTimeMillis());
    }
}
