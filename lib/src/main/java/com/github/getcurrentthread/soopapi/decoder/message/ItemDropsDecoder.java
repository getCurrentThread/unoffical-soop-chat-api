package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.ItemDropsEvent;

public class ItemDropsDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 5;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new ItemDropsEvent(
                parts[1],
                parts[2],
                parts[3],
                parts[4],
                ChatEvent.ITEM_DROPS,
                raw,
                System.currentTimeMillis());
    }
}
