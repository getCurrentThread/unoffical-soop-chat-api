package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.QuitChannelEvent;

public class QuitChannelDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new QuitChannelEvent(
                Integer.parseInt(parts[2]),
                Integer.parseInt(parts[3]),
                parts[4],
                parts[5],
                parts[6],
                ChatEvent.QUIT_CHANNEL,
                raw,
                System.currentTimeMillis());
    }
}
