package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BanWordEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;

public class BanWordDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 2;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new BanWordEvent(
                parts[0], parts[1].split(","), ChatEvent.BAN_WORD, raw, System.currentTimeMillis());
    }
}
