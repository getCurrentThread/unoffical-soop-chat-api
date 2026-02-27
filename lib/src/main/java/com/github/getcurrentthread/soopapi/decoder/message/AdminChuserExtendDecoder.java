package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.AdminChuserExtendEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;

public class AdminChuserExtendDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new AdminChuserExtendEvent(
                ChatEvent.ADMIN_CHUSER_EXTEND, raw, System.currentTimeMillis());
    }
}
