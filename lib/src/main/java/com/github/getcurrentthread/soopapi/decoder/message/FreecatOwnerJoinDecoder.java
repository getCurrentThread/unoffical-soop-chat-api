package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.FreecatOwnerJoinEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class FreecatOwnerJoinDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 7;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new FreecatOwnerJoinEvent(
                parts[0],
                parts[1],
                SOOPChatUtils.safeParseInt(parts[3], 0),
                parts[4],
                parts[6],
                ChatEvent.FREECAT_OWNER_JOIN,
                raw,
                System.currentTimeMillis());
    }
}
