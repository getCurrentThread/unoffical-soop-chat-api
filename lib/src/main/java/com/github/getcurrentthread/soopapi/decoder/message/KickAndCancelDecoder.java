package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class KickAndCancelDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", Integer.parseInt(parts[0]));
        result.put("userId", parts[1]);
        result.put("userNickname", parts[2]);
        return result;
    }
}