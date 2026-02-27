package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.TopFanSubEvent;

public class TopFanSubDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new TopFanSubEvent(ChatEvent.TOP_FAN_SUB, raw, System.currentTimeMillis());
    }
}
