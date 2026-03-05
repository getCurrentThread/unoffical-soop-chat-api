package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.NoneTypeEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class NoneTypeDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        int value = parts.length > 0 ? SOOPChatUtils.safeParseInt(parts[0], 0) : 0;
        return new NoneTypeEvent(value, ChatEvent.NONE_TYPE, raw, System.currentTimeMillis());
    }
}
