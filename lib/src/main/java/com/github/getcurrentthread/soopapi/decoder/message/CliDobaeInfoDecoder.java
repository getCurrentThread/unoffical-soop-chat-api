package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.CliDobaeInfoEvent;

public class CliDobaeInfoDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new CliDobaeInfoEvent(
                Integer.parseInt(parts[0]),
                parts[1],
                ChatEvent.CLI_DOBAE_INFO,
                raw,
                System.currentTimeMillis());
    }
}
