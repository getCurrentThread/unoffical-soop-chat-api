package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.GemItemSendEvent;

public class GemItemDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 4;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new GemItemSendEvent(
                parts[1],
                parts[2],
                parts[3],
                ChatEvent.GEM_ITEM_SEND,
                raw,
                System.currentTimeMillis());
    }
}
