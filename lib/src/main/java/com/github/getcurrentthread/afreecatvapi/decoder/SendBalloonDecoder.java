package com.github.getcurrentthread.afreecatvapi.decoder;

import java.util.HashMap;
import java.util.Map;

public class SendBalloonDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("bjId", parts[1]);
        result.put("senderId", parts[2]);
        result.put("senderNickname", parts[3]);
        result.put("balloonCount", Integer.valueOf(parts[4]));
        result.put("balloonInfo", parts[8]);
        return result;
    }
}