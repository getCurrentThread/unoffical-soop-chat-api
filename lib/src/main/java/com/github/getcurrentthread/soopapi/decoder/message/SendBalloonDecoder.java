package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.SendBalloonEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class SendBalloonDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 10;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new SendBalloonEvent(
                parts[0],
                parts[1],
                parts[2],
                SOOPChatUtils.safeParseInt(parts[3], 0),
                SOOPChatUtils.safeParseInt(parts[4], 0),
                parts[7],
                "1".equals(parts[8]),
                SOOPChatUtils.safeParseInt(parts[9], 0),
                parts.length > 10 ? parts[10] : "",
                ChatEvent.SEND_BALLOON,
                raw,
                System.currentTimeMillis());
    }
}
