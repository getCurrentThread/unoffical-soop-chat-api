package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BanWordEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;

public class BanWordDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new BanWordEvent(
                parts[0], parts[1].split(","), ChatEvent.BAN_WORD, raw, System.currentTimeMillis());
    }
}
