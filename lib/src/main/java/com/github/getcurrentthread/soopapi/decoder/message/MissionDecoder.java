package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.Map;

import com.github.getcurrentthread.soopapi.util.GsonUtil;

public class MissionDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = GsonUtil.fromJson(parts[0]);
        return result;
    }
}
