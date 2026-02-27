package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.ChocolateEvent;

public class ChocolateDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new ChocolateEvent(
                parts[1],
                parts[2],
                parts[3],
                Integer.parseInt(parts[4]),
                ChatEvent.CHOCOLATE,
                raw,
                System.currentTimeMillis());
    }
}
