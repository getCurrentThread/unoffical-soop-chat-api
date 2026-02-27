package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.PaidItemEvent;

public class PaidItemDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new PaidItemEvent(
                Integer.parseInt(parts[1]),
                parts[2],
                parts[4],
                parts[5],
                parts[6],
                Integer.parseInt(parts[7]),
                ChatEvent.PAID_ITEM,
                raw,
                System.currentTimeMillis());
    }
}
