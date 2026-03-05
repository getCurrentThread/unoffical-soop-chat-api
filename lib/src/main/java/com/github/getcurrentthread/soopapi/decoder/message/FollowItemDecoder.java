package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.FollowItemEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class FollowItemDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 5;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new FollowItemEvent(
                SOOPChatUtils.safeParseInt(parts[0], 0),
                parts[1],
                parts[2],
                parts[3],
                SOOPChatUtils.safeParseInt(parts[4], 0),
                ChatEvent.FOLLOW_ITEM,
                raw,
                System.currentTimeMillis());
    }
}
