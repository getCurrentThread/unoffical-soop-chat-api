package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.Map;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.MissionSettleEvent;
import com.github.getcurrentthread.soopapi.util.GsonUtil;

public class MissionSettleDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        Map<String, Object> data = GsonUtil.fromJson(parts[0]);
        return new MissionSettleEvent(
                data, ChatEvent.MISSION_SETTLE, raw, System.currentTimeMillis());
    }
}
