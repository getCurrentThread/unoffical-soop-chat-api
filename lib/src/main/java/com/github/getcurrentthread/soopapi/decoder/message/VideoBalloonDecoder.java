package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.VideoBalloonEvent;

public class VideoBalloonDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new VideoBalloonEvent(
                parts[0],
                parts[1],
                parts[2],
                parts[3],
                Integer.parseInt(parts[4]),
                Integer.parseInt(parts[5]),
                Integer.parseInt(parts[7]),
                parts[8],
                parts[12],
                "1".equals(parts[13]),
                parts.length > 14 ? parts[14] : "",
                ChatEvent.VIDEO_BALLOON,
                raw,
                System.currentTimeMillis());
    }
}
