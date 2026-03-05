package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.SetSubBjEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class SetSubBjDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 4;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new SetSubBjEvent(
                parts[0],
                parts[1],
                SOOPChatUtils.safeParseInt(parts[2], 0),
                parts[3],
                ChatEvent.SET_SUB_BJ,
                raw,
                System.currentTimeMillis());
    }
}
