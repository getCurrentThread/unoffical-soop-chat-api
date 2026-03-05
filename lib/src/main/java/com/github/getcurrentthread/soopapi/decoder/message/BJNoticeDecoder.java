package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.BjNoticeEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class BJNoticeDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 4;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new BjNoticeEvent(
                SOOPChatUtils.safeParseInt(parts[1], 0),
                parts[3],
                ChatEvent.BJ_NOTICE,
                raw,
                System.currentTimeMillis());
    }
}
