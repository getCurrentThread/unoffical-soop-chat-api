package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.SendSubscriptionEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class GiftSubscriptionDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 14;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new SendSubscriptionEvent(
                parts[1],
                parts[2],
                parts[3],
                parts[4],
                parts[5],
                parts[6],
                SOOPChatUtils.safeParseInt(parts[7], 0),
                parts[8],
                SOOPChatUtils.safeParseInt(parts[9], 0),
                parts[10],
                parts[11],
                SOOPChatUtils.safeParseInt(parts[12], 0),
                SOOPChatUtils.safeParseInt(parts[13], 0),
                ChatEvent.SEND_SUBSCRIPTION,
                raw,
                System.currentTimeMillis());
    }
}
