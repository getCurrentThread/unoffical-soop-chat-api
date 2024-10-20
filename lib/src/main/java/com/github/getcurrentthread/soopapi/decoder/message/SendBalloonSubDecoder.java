package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class SendBalloonSubDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("bjId", parts[1]);
        result.put("senderId", parts[3]);
        result.put("senderNickname", parts[4]);
        result.put("count", Integer.parseInt(parts[5]));
        result.put("fanOrder", Integer.parseInt(parts[6]));
        result.put("fileName", parts[8]);
        result.put("isDefault", "1".equals(parts[9]));
        result.put("isTopFan", Integer.parseInt(parts[10]));
        return result;
    }
}
