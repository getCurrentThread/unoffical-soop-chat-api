package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.SendAdminNoticeEvent;

public class SendAdminNoticeDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new SendAdminNoticeEvent(
                parts[0], ChatEvent.SEND_ADMIN_NOTICE, raw, System.currentTimeMillis());
    }
}
