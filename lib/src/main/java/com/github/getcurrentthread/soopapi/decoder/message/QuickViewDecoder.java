package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.QuickViewEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class QuickViewDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 6;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new QuickViewEvent(
                parts[1],
                parts[2],
                parts[3],
                parts[4],
                SOOPChatUtils.safeParseInt(parts[5], 0),
                ChatEvent.SEND_QUICK_VIEW,
                raw,
                System.currentTimeMillis());
    }
}
