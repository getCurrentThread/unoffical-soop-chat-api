package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class StationAdconDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        if (parts.length >= 7) {
            result.put("bjId", parts[0]);
            result.put("userId", parts[1]);
            result.put("userNickName", parts[2]);
            result.put("adconCount", Integer.parseInt(parts[3]));
            result.put("isDefault", parts[4]);
            result.put("adconMsg", parts[5]);
            result.put("chatNumber", parts[6]);
        }
        return result;
    }
}
