package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.Map;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.AdInBroadJsonEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.util.GsonUtil;

public class AdInBroadJsonDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        Map<String, Object> data = GsonUtil.fromJson(parts[0]);
        return new AdInBroadJsonEvent(
                data, ChatEvent.AD_IN_BROAD_JSON, raw, System.currentTimeMillis());
    }
}
