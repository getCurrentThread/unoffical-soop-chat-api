package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.SendBalloonEvent;

public class SendBalloonDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new SendBalloonEvent(
                parts[0],
                parts[1],
                parts[2],
                Integer.parseInt(parts[3]),
                Integer.parseInt(parts[4]),
                parts[7],
                "1".equals(parts[8]),
                Integer.parseInt(parts[9]),
                parts.length > 10 ? parts[10] : "",
                ChatEvent.SEND_BALLOON,
                raw,
                System.currentTimeMillis());
    }
}
