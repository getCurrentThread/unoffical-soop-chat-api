package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.GiftOGQEmoticonEvent;

public class GiftOGQEmoticonDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 7;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new GiftOGQEmoticonEvent(
                parts[1],
                parts[2],
                parts[3],
                parts[4],
                parts[5],
                parts[6],
                ChatEvent.OGQ_EMOTICON_GIFT,
                raw,
                System.currentTimeMillis());
    }
}
