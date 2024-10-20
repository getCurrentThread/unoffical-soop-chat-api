package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class SetDumbDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("userId", parts[0]);
        result.put("userInfo", parts[1]);
        result.put("dumbTime", Integer.parseInt(parts[2]));
        result.put("dumbCount", Integer.parseInt(parts[3]));
        result.put("adminId", parts[4]);
        result.put("adminNickname", parts[5]);
        return result;
    }
}