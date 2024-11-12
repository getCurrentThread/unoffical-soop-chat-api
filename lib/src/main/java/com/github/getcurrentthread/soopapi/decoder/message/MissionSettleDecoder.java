package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.util.GsonUtil;

import java.util.Map;

public class MissionSettleDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = GsonUtil.fromJson(parts[0]);
        return result;
    }
}