package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.VideoBalloonEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class VideoBalloonDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 14;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new VideoBalloonEvent(
                parts[0],
                parts[1],
                parts[2],
                parts[3],
                SOOPChatUtils.safeParseInt(parts[4], 0),
                SOOPChatUtils.safeParseInt(parts[5], 0),
                SOOPChatUtils.safeParseInt(parts[7], 0),
                parts[8],
                parts[12],
                "1".equals(parts[13]),
                parts.length > 14 ? parts[14] : "",
                ChatEvent.VIDEO_BALLOON,
                raw,
                System.currentTimeMillis());
    }
}
