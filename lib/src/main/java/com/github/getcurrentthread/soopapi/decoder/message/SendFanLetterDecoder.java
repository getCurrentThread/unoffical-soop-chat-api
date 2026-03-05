package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.SendFanLetterEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class SendFanLetterDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 9;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new SendFanLetterEvent(
                parts[0],
                parts[1],
                parts[2],
                parts[3],
                SOOPChatUtils.safeParseInt(parts[5], 0),
                SOOPChatUtils.safeParseInt(parts[7], 0),
                parts[8],
                ChatEvent.SEND_FAN_LETTER,
                raw,
                System.currentTimeMillis());
    }
}
