package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.SnsModeEvent;

public class SnsModeDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new SnsModeEvent(
                Integer.parseInt(parts[0]), ChatEvent.SNS_MODE, raw, System.currentTimeMillis());
    }
}
