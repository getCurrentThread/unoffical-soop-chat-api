package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class ItemDropsDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("bjId", parts[1]);
        result.put("dropsName", parts[2]);
        result.put("dropsMsg", parts[3]);
        result.put("dropsImgUrl", parts[4]);
        return result;
    }
}