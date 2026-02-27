package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.UseQuickViewEvent;

public class UseQuickViewDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new UseQuickViewEvent(ChatEvent.USE_QUICK_VIEW, raw, System.currentTimeMillis());
    }
}
