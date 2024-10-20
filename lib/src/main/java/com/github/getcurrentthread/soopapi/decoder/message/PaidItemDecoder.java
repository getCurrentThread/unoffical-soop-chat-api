package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class PaidItemDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("itemType", Integer.parseInt(parts[1]));
        result.put("bjId", parts[2]);
        result.put("buyerId", parts[4]);
        result.put("buyerNickname", parts[5]);
        result.put("itemName", parts[6]);
        result.put("itemCount", Integer.parseInt(parts[7]));
        return result;
    }
}