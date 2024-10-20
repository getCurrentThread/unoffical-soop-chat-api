package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class GemItemDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("receiverId", parts[1]);
        result.put("receiverNick", parts[2]);
        result.put("itemName", parts[3]);
        return result;
    }
}