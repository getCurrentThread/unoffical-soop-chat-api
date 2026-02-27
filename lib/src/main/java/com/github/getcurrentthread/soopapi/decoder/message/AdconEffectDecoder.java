package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.AdconEffectEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;

public class AdconEffectDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new AdconEffectEvent(
                Integer.parseInt(parts[0]),
                parts[1],
                parts[2],
                parts[3],
                parts[4],
                parts[5],
                parts[6],
                parts[7],
                parts[8],
                Integer.valueOf(parts[9]),
                Integer.valueOf(parts[10]),
                Integer.valueOf(parts[11]),
                Integer.valueOf(parts[12]),
                Integer.valueOf(parts[13]),
                ChatEvent.ADCON_EFFECT,
                raw,
                System.currentTimeMillis());
    }
}
