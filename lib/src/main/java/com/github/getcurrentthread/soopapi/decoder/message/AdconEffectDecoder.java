package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.AdconEffectEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class AdconEffectDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 14;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new AdconEffectEvent(
                SOOPChatUtils.safeParseInt(parts[0], 0),
                parts[1],
                parts[2],
                parts[3],
                parts[4],
                parts[5],
                parts[6],
                parts[7],
                parts[8],
                SOOPChatUtils.safeParseInt(parts[9], 0),
                SOOPChatUtils.safeParseInt(parts[10], 0),
                SOOPChatUtils.safeParseInt(parts[11], 0),
                SOOPChatUtils.safeParseInt(parts[12], 0),
                SOOPChatUtils.safeParseInt(parts[13], 0),
                ChatEvent.ADCON_EFFECT,
                raw,
                System.currentTimeMillis());
    }
}
