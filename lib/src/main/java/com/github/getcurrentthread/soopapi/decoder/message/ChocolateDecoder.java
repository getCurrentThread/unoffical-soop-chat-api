package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class ChocolateDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("bjId", parts[1]);
        result.put("senderId", parts[2]);
        result.put("senderNickname", parts[3]);
        result.put("count", Integer.parseInt(parts[4]));
        return result;
    }
}