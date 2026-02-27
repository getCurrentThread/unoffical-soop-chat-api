package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.FreecatOwnerJoinEvent;

public class FreecatOwnerJoinDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new FreecatOwnerJoinEvent(
                parts[0],
                parts[1],
                Integer.parseInt(parts[3]),
                parts[4],
                parts[6],
                ChatEvent.FREECAT_OWNER_JOIN,
                raw,
                System.currentTimeMillis());
    }
}
