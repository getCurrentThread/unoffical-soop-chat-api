package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.TranslationEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class TranslationDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 5;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new TranslationEvent(
                SOOPChatUtils.safeParseInt(parts[0], 0),
                SOOPChatUtils.safeParseInt(parts[1], 0),
                parts[2],
                SOOPChatUtils.safeParseInt(parts[3], 0),
                SOOPChatUtils.safeParseInt(parts[4], 0),
                ChatEvent.TRANSLATION,
                raw,
                System.currentTimeMillis());
    }
}
