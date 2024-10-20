package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class GiftOGQEmoticonDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("senderId", parts[1]);
        result.put("senderNick", parts[2]);
        result.put("receivedId", parts[3]);
        result.put("receivedNick", parts[4]);
        result.put("ogqTitle", parts[5]);
        result.put("ogqImageUrl", parts[6]);
        return result;
    }
}