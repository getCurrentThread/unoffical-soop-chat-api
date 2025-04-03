package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class FreecatOwnerJoinDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("chatNo", parts[0]);
        result.put("bjId", parts[1]);
        result.put("maxSubBjCount", Integer.parseInt(parts[3]));
        result.put("familyNickname", parts[4]);
        result.put("userFlag", parts[6]);
        return result;
    }
}
