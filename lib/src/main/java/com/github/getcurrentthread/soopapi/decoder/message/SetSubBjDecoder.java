package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.SetSubBjEvent;

public class SetSubBjDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new SetSubBjEvent(
                parts[0],
                parts[1],
                Integer.parseInt(parts[2]),
                parts[3],
                ChatEvent.SET_SUB_BJ,
                raw,
                System.currentTimeMillis());
    }
}
