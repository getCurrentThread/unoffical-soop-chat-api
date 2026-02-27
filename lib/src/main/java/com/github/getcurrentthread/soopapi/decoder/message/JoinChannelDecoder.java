package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.JoinChannelEvent;

public class JoinChannelDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new JoinChannelEvent(
                parts[0],
                parts[1],
                Integer.parseInt(parts[3]),
                parts[4],
                parts[6],
                ChatEvent.JOIN_CHANNEL,
                raw,
                System.currentTimeMillis());
    }
}
