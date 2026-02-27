package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.VodBalloonEvent;

public class VODBalloonDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new VodBalloonEvent(
                parts[1],
                parts[2],
                parts[3],
                Integer.valueOf(parts[4]),
                ChatEvent.VOD_BALLOON,
                raw,
                System.currentTimeMillis());
    }
}
