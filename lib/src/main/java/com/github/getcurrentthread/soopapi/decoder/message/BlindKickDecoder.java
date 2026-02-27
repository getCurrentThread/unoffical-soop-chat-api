package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.BlindKickEvent;

public class BlindKickDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new BlindKickEvent(
                parts[0],
                parts[1],
                Integer.parseInt(parts[2]),
                ChatEvent.BLIND_KICK,
                raw,
                System.currentTimeMillis());
    }
}
