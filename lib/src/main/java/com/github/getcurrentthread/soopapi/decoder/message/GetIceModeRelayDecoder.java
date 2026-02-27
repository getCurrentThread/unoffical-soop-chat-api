package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.GetIceModeRelayEvent;

public class GetIceModeRelayDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new GetIceModeRelayEvent(
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                ChatEvent.GET_ICE_MODE_RELAY,
                raw,
                System.currentTimeMillis());
    }
}
