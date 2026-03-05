package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.EmoticonTicketEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class EmoticonTicketDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        int value = parts.length > 0 ? SOOPChatUtils.safeParseInt(parts[0], 0) : 0;
        return new EmoticonTicketEvent(
                value, ChatEvent.EMOTICON_TICKET, raw, System.currentTimeMillis());
    }
}
