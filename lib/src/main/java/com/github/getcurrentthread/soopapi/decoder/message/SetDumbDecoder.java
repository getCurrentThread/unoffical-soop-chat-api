package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.SetDumbEvent;

public class SetDumbDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new SetDumbEvent(
                parts[0],
                parts[1],
                Integer.parseInt(parts[2]),
                Integer.parseInt(parts[3]),
                parts[4],
                Integer.parseInt(parts[5]),
                parts[6],
                parts[7],
                ChatEvent.SET_DUMB,
                raw,
                System.currentTimeMillis());
    }
}
