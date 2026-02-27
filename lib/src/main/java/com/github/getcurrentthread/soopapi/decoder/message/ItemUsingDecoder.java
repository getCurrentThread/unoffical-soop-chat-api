package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.ItemUsingEvent;

public class ItemUsingDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new ItemUsingEvent(
                parts[1],
                Integer.parseInt(parts[2]),
                Integer.parseInt(parts[3]),
                ChatEvent.ITEM_USING,
                raw,
                System.currentTimeMillis());
    }
}
