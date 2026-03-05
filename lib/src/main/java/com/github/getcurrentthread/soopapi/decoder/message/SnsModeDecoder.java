package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.SnsModeEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class SnsModeDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 1;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new SnsModeEvent(
                SOOPChatUtils.safeParseInt(parts[0], 0),
                ChatEvent.SNS_MODE,
                raw,
                System.currentTimeMillis());
    }
}
