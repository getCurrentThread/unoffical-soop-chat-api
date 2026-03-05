package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.KickAndCancelEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class KickAndCancelDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 3;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new KickAndCancelEvent(
                SOOPChatUtils.safeParseInt(parts[0], 0),
                parts[1],
                parts[2],
                ChatEvent.KICK_AND_CANCEL,
                raw,
                System.currentTimeMillis());
    }
}
