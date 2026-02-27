package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.SnsMessageEvent;

public class SnsMessageDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new SnsMessageEvent(
                parts[0], ChatEvent.SNS_MESSAGE, raw, System.currentTimeMillis());
    }
}
