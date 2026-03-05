package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.ItemUsingEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class ItemUsingDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 4;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new ItemUsingEvent(
                parts[1],
                SOOPChatUtils.safeParseInt(parts[2], 0),
                SOOPChatUtils.safeParseInt(parts[3], 0),
                ChatEvent.ITEM_USING,
                raw,
                System.currentTimeMillis());
    }
}
