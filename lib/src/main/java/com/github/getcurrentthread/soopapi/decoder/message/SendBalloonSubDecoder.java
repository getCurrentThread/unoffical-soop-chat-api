package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.SendBalloonSubEvent;

public class SendBalloonSubDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new SendBalloonSubEvent(
                parts[1],
                parts[3],
                parts[4],
                Integer.parseInt(parts[5]),
                Integer.parseInt(parts[6]),
                parts[8],
                "1".equals(parts[9]),
                Integer.parseInt(parts[10]),
                ChatEvent.SEND_BALLOON_SUB,
                raw,
                System.currentTimeMillis());
    }
}
