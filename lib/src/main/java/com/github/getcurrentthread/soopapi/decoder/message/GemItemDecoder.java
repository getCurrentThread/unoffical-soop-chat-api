package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.GemItemSendEvent;

public class GemItemDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new GemItemSendEvent(
                parts[1],
                parts[2],
                parts[3],
                ChatEvent.GEM_ITEM_SEND,
                raw,
                System.currentTimeMillis());
    }
}
