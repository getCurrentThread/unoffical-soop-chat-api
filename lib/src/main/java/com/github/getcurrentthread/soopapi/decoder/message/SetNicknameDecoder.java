package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.SetNicknameEvent;

public class SetNicknameDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new SetNicknameEvent(
                parts[0],
                parts[1],
                Integer.parseInt(parts[2]),
                parts[3],
                parts.length > 4 ? parts[4] : "",
                ChatEvent.SET_NICKNAME,
                raw,
                System.currentTimeMillis());
    }
}
