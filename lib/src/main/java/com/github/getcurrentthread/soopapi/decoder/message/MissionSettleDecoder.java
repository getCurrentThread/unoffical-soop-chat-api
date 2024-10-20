package com.github.getcurrentthread.soopapi.decoder.message;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class MissionSettleDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
//        try {
        Map<String, Object> settleData = new Gson().fromJson(parts[0], Map.class);
        result.put("fanOrder", settleData.get("fanOrder"));
        result.put("list", settleData.get("list"));
//        } catch (JsonSyntaxException e) {
//            // Handle JSON parsing error
//        }
        return result;
    }
}