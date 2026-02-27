package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.SendSubscriptionEvent;

public class GiftSubscriptionDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new SendSubscriptionEvent(
                parts[1],
                parts[2],
                parts[3],
                parts[4],
                parts[5],
                parts[6],
                Integer.valueOf(parts[7]),
                parts[8],
                Integer.valueOf(parts[9]),
                parts[10],
                parts[11],
                Integer.valueOf(parts[12]),
                Integer.valueOf(parts[13]),
                ChatEvent.SEND_SUBSCRIPTION,
                raw,
                System.currentTimeMillis());
    }
}
