package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class SetSubBjDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("userId", parts[0]);
        result.put("flag", parts[1]);
        result.put("hide", Integer.parseInt(parts[2]));
        result.put("nickname", parts[3]);
        return result;
    }
}