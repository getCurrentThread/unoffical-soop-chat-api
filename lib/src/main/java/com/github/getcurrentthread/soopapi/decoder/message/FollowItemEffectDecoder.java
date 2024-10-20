package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class FollowItemEffectDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("bjId", parts[0]);
        result.put("sendId", parts[1]);
        result.put("sendNick", parts[2]);
        result.put("month", Integer.parseInt(parts[3]));
        result.put("chatNo", Integer.parseInt(parts[4]));
        return result;
    }
}