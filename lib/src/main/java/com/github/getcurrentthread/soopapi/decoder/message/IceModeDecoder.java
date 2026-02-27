package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.IceModeEvent;

public class IceModeDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new IceModeEvent(
                Integer.parseInt(parts[0]), ChatEvent.ICE_MODE, raw, System.currentTimeMillis());
    }
}
