package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.SetUserFlagEvent;

public class SetUserFlagDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 6;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new SetUserFlagEvent(
                parts[0],
                parts[1],
                parts[2],
                parts[5],
                ChatEvent.SET_USER_FLAG,
                raw,
                System.currentTimeMillis());
    }
}
