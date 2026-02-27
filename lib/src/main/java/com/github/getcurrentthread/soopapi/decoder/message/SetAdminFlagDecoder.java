package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.SetAdminFlagEvent;

public class SetAdminFlagDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new SetAdminFlagEvent(
                parts[0], ChatEvent.SET_ADMIN_FLAG, raw, System.currentTimeMillis());
    }
}
