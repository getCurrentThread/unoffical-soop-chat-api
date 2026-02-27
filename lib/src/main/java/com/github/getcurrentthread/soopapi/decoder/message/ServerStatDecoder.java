package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.ServerStatEvent;

public class ServerStatDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new ServerStatEvent(
                parts[0], ChatEvent.SERVER_STAT, raw, System.currentTimeMillis());
    }
}
