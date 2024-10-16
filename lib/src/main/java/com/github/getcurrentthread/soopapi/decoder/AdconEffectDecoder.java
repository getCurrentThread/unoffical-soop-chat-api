package com.github.getcurrentthread.soopapi.decoder;

import java.util.HashMap;
import java.util.Map;

public class AdconEffectDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("bjId", parts[1]);
        result.put("senderId", parts[2]);
        result.put("senderNickname", parts[3]);
        result.put("message", parts[4]);
        result.put("message2", parts[5]);
        result.put("title", parts[6]);
        result.put("urlImg", parts[7]);
        result.put("urlDefault", parts[8]);
        result.put("adconCount", Integer.valueOf(parts[9]));
        result.put("fanOrder", Integer.valueOf(parts[10]));
        result.put("isTopFan", Integer.valueOf(parts[11]));
        result.put("isFanChief", Integer.valueOf(parts[12]));
        result.put("isSubRoom", Integer.valueOf(parts[13]));
        return result;
    }
}