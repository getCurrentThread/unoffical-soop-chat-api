package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.SetDumbEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class SetDumbDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 8;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new SetDumbEvent(
                parts[0],
                parts[1],
                SOOPChatUtils.safeParseInt(parts[2], 0),
                SOOPChatUtils.safeParseInt(parts[3], 0),
                parts[4],
                SOOPChatUtils.safeParseInt(parts[5], 0),
                parts[6],
                parts[7],
                ChatEvent.SET_DUMB,
                raw,
                System.currentTimeMillis());
    }
}
