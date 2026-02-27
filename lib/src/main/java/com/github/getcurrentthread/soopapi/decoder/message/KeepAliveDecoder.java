package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.KeepAliveEvent;

public class KeepAliveDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new KeepAliveEvent(ChatEvent.KEEP_ALIVE, raw, System.currentTimeMillis());
    }
}
