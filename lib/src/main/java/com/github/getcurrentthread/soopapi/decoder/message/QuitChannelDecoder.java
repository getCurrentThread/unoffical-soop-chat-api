package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.QuitChannelEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class QuitChannelDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 7;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new QuitChannelEvent(
                SOOPChatUtils.safeParseInt(parts[2], 0),
                SOOPChatUtils.safeParseInt(parts[3], 0),
                parts[4],
                parts[5],
                parts[6],
                ChatEvent.QUIT_CHANNEL,
                raw,
                System.currentTimeMillis());
    }
}
