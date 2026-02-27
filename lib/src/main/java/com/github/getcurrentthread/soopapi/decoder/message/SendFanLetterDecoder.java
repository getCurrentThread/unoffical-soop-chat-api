package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.SendFanLetterEvent;

public class SendFanLetterDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new SendFanLetterEvent(
                parts[0],
                parts[1],
                parts[2],
                parts[3],
                Integer.parseInt(parts[5]),
                Integer.parseInt(parts[7]),
                parts[8],
                ChatEvent.SEND_FAN_LETTER,
                raw,
                System.currentTimeMillis());
    }
}
