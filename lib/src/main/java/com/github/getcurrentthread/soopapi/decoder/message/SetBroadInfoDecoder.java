package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.SetBroadInfoEvent;

public class SetBroadInfoDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new SetBroadInfoEvent(ChatEvent.SET_BROAD_INFO, raw, System.currentTimeMillis());
    }
}
