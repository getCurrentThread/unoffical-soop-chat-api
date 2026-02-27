package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.NotifyMobBroadPauseEvent;

public class NotifyMobBroadPauseDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new NotifyMobBroadPauseEvent(
                Integer.parseInt(parts[0]),
                ChatEvent.NOTIFY_MOBBROAD_PAUSE,
                raw,
                System.currentTimeMillis());
    }
}
