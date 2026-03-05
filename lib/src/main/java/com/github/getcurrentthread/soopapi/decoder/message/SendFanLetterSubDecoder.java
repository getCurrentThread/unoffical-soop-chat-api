package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.SendFanLetterSubEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class SendFanLetterSubDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 10;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new SendFanLetterSubEvent(
                parts[1],
                parts[2],
                parts[3],
                parts[4],
                SOOPChatUtils.safeParseInt(parts[6], 0),
                SOOPChatUtils.safeParseInt(parts[8], 0),
                parts[9],
                ChatEvent.SEND_FAN_LETTER_SUB,
                raw,
                System.currentTimeMillis());
    }
}
