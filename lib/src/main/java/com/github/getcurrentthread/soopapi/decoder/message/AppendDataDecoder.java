package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.AppendDataEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;

public class AppendDataDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 1;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new AppendDataEvent(
                parts[0], ChatEvent.APPEND_DATA, raw, System.currentTimeMillis());
    }
}
