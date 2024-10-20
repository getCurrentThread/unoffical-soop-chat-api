package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class QuickViewDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("senderId", parts[1]);
        result.put("senderNickname", parts[2]);
        result.put("receiverId", parts[3]);
        result.put("receiverNickname", parts[4]);
        result.put("itemType", Integer.valueOf(parts[5]));
        return result;
    }
}