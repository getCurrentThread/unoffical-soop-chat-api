package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.SetChannelNameEvent;

public class SetChannelNameDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new SetChannelNameEvent(
                parts[0], ChatEvent.SET_CHANNEL_NAME, raw, System.currentTimeMillis());
    }
}
