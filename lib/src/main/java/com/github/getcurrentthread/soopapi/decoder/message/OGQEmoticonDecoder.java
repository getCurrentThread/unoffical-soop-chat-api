package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class OGQEmoticonDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        if (parts.length >= 6) {
            result.put("chatNo", parts[0]);
            result.put("message", parts[1]);
            result.put("groupId", parts[2]);
            result.put("subId", parts[3]);
            result.put("version", parts[4]);
            result.put("userInfo", parts[5]);
        }
        // 추가 정보가 있다면 처리
        if (parts.length >= 9) {
            result.put("color", parts[6]);
            result.put("chatLang", parts[7]);
            result.put("type", parts[8]);
        }
        return result;
    }
}
