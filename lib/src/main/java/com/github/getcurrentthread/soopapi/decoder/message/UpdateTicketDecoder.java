package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.UpdateTicketEvent;

public class UpdateTicketDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new UpdateTicketEvent(ChatEvent.UPDATE_TICKET, raw, System.currentTimeMillis());
    }
}
