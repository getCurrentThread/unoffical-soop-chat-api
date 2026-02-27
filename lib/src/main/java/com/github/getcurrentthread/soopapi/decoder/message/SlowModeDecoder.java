package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.SlowModeEvent;

public class SlowModeDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new SlowModeEvent(
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                ChatEvent.SLOW_MODE,
                raw,
                System.currentTimeMillis());
    }
}
