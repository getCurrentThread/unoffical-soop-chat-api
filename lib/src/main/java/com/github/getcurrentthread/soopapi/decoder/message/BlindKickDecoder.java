package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.BlindKickEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class BlindKickDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 3;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new BlindKickEvent(
                parts[0],
                parts[1],
                SOOPChatUtils.safeParseInt(parts[2], 0),
                ChatEvent.BLIND_KICK,
                raw,
                System.currentTimeMillis());
    }
}
