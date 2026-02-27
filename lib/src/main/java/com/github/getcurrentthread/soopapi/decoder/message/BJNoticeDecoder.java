package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.BjNoticeEvent;

public class BJNoticeDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new BjNoticeEvent(
                Integer.parseInt(parts[1]),
                parts[3],
                ChatEvent.BJ_NOTICE,
                raw,
                System.currentTimeMillis());
    }
}
