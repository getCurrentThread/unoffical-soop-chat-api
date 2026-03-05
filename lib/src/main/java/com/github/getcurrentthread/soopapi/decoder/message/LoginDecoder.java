package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.LoginEvent;

public class LoginDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 2;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new LoginEvent(parts[0], parts[1], ChatEvent.LOGIN, raw, System.currentTimeMillis());
    }
}
