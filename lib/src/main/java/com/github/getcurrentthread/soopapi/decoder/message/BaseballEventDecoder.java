package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseballEventEvent;

public class BaseballEventDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new BaseballEventEvent(
                parts[0], ChatEvent.BASEBALL_EVENT, raw, System.currentTimeMillis());
    }
}
