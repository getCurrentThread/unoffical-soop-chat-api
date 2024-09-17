package com.github.getcurrentthread.afreecatvapi.decoder;

import java.util.HashMap;
import java.util.Map;

public class VideoBalloonDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("chatNo", parts[0]);
        result.put("bjId", parts[1]);
        result.put("senderId", parts[2]);
        result.put("senderNickname", parts[3]);
        result.put("balloonCount", parts[4]);
        result.put("fanOrder", Integer.valueOf(parts[5]));
        result.put("isTopFan", parts[7]);
        result.put("relay", parts[8]);
        result.put("fileName", parts[12]);
        result.put("isDefault", Integer.valueOf(parts[13]) == 1);
        return result;
    }
}