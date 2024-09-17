package com.github.getcurrentthread.afreecatvapi.decoder;

import java.util.HashMap;
import java.util.Map;

public class VODBalloonDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("bjId", parts[1]);
        result.put("userId", parts[2]);
        result.put("userNickname", parts[3]);
        result.put("balloonCount", Integer.valueOf(parts[4]));
        return result;
    }
}