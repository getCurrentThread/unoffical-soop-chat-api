package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class SendBalloonDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("bjId", parts[0]);
        result.put("senderId", parts[1]);
        result.put("senderNickname", parts[2]);
        result.put("count", Integer.parseInt(parts[3]));
        result.put("fanOrder", Integer.parseInt(parts[4]));
        result.put("fileName", parts[7]);
        result.put("isDefault", "1".equals(parts[8]));
        result.put("isTopFan", Integer.parseInt(parts[9]));
        return result;
    }
}