package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.SendBalloonSubEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class SendBalloonSubDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 11;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new SendBalloonSubEvent(
                parts[1],
                parts[3],
                parts[4],
                SOOPChatUtils.safeParseInt(parts[5], 0),
                SOOPChatUtils.safeParseInt(parts[6], 0),
                parts[8],
                "1".equals(parts[9]),
                SOOPChatUtils.safeParseInt(parts[10], 0),
                ChatEvent.SEND_BALLOON_SUB,
                raw,
                System.currentTimeMillis());
    }
}
