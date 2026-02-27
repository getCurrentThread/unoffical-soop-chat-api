package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.ItemDropsEvent;

public class ItemDropsDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
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
