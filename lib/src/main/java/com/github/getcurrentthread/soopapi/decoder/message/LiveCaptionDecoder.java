package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.LiveCaptionEvent;

public class LiveCaptionDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new LiveCaptionEvent(
                parts[0], ChatEvent.LIVE_CAPTION, raw, System.currentTimeMillis());
    }
}
