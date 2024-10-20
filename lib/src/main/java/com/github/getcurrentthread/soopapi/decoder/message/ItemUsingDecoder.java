package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class ItemUsingDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("bjId", parts[1]);
        result.put("itemType", Integer.parseInt(parts[2]));
        result.put("remainTime", Integer.parseInt(parts[3]));
        return result;
    }
}