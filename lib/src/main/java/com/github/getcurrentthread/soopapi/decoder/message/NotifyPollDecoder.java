package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.NotifyPollEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class NotifyPollDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 4;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new NotifyPollEvent(
                SOOPChatUtils.safeParseInt(parts[0], 0),
                parts[1],
                SOOPChatUtils.safeParseInt(parts[2], 0),
                SOOPChatUtils.safeParseInt(parts[3], 0),
                ChatEvent.NOTIFY_POLL,
                raw,
                System.currentTimeMillis());
    }
}
