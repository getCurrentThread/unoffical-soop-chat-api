package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.TopClanSubEvent;

public class TopClanSubDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new TopClanSubEvent(ChatEvent.TOP_CLAN_SUB, raw, System.currentTimeMillis());
    }
}
