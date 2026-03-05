package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.ChocolateEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class ChocolateDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 5;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new ChocolateEvent(
                parts[1],
                parts[2],
                parts[3],
                SOOPChatUtils.safeParseInt(parts[4], 0),
                ChatEvent.CHOCOLATE,
                raw,
                System.currentTimeMillis());
    }
}
