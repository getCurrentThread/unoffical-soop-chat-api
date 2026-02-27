package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.NotifyPollEvent;

public class NotifyPollDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new NotifyPollEvent(
                Integer.parseInt(parts[0]),
                parts[1],
                Integer.parseInt(parts[2]),
                Integer.parseInt(parts[3]),
                ChatEvent.NOTIFY_POLL,
                raw,
                System.currentTimeMillis());
    }
}
