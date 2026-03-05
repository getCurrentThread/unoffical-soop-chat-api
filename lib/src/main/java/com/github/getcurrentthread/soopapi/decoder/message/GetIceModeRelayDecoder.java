package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.GetIceModeRelayEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class GetIceModeRelayDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 2;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new GetIceModeRelayEvent(
                SOOPChatUtils.safeParseInt(parts[0], 0),
                SOOPChatUtils.safeParseInt(parts[1], 0),
                ChatEvent.GET_ICE_MODE_RELAY,
                raw,
                System.currentTimeMillis());
    }
}
