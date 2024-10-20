package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class BlindKickDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("userId", parts[0]);
        result.put("userNickname", parts[1]);
        result.put("blindTime", Integer.parseInt(parts[2]));
        return result;
    }
}