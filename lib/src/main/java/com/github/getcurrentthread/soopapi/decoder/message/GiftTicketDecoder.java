package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.GiftTicketEvent;

public class GiftTicketDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 6;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new GiftTicketEvent(
                parts[1],
                parts[2],
                parts[3],
                parts[4],
                parts[5],
                ChatEvent.GIFT_TICKET,
                raw,
                System.currentTimeMillis());
    }
}
