package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.KickAndCancelEvent;

public class KickAndCancelDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new KickAndCancelEvent(
                Integer.parseInt(parts[0]),
                parts[1],
                parts[2],
                ChatEvent.KICK_AND_CANCEL,
                raw,
                System.currentTimeMillis());
    }
}
