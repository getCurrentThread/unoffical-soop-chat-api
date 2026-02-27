package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.QuickViewEvent;

public class QuickViewDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new QuickViewEvent(
                parts[1],
                parts[2],
                parts[3],
                parts[4],
                Integer.valueOf(parts[5]),
                ChatEvent.SEND_QUICK_VIEW,
                raw,
                System.currentTimeMillis());
    }
}
