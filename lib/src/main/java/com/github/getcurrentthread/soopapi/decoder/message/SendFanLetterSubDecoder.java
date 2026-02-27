package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.SendFanLetterSubEvent;

public class SendFanLetterSubDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new SendFanLetterSubEvent(
                parts[1],
                parts[2],
                parts[3],
                parts[4],
                Integer.parseInt(parts[6]),
                Integer.parseInt(parts[8]),
                parts[9],
                ChatEvent.SEND_FAN_LETTER_SUB,
                raw,
                System.currentTimeMillis());
    }
}
