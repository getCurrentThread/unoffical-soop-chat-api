package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class SetUserFlagDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("userId", parts[1]);
        result.put("userNickname", parts[2]);
        result.put("oldFlag", parts[0]);
        result.put("newFlag", parts[5]);
        return result;
    }
}