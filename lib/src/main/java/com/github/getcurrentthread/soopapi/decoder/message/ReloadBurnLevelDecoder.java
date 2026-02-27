package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.ReloadBurnLevelEvent;

public class ReloadBurnLevelDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new ReloadBurnLevelEvent(
                Integer.parseInt(parts[0]),
                ChatEvent.RELOAD_BURN_LEVEL,
                raw,
                System.currentTimeMillis());
    }
}
