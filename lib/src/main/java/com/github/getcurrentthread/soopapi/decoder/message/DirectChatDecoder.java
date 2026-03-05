package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.DirectChatEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class DirectChatDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 8;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new DirectChatEvent(
                parts[0],
                parts[1],
                parts[2],
                SOOPChatUtils.safeParseInt(parts[3], 0),
                parts[5],
                parts[6],
                parts[7],
                ChatEvent.DIRECT_CHAT,
                raw,
                System.currentTimeMillis());
    }
}
