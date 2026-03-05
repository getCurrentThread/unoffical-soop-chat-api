package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.Map;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.MissionSettleEvent;
import com.github.getcurrentthread.soopapi.util.GsonUtil;

public class MissionSettleDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 1;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        try {
            Map<String, Object> data = GsonUtil.fromJson(parts[0]);
            if (data == null) {
                return null;
            }
            return new MissionSettleEvent(
                    data, ChatEvent.MISSION_SETTLE, raw, System.currentTimeMillis());
        } catch (Exception e) {
            return null;
        }
    }
}
