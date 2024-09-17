package com.github.getcurrentthread.afreecatvapi.decoder;

import java.util.HashMap;
import java.util.Map;

public class ManagerChatDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", parts[0]);
        result.put("senderId", parts[1]);
        result.put("isAdmin", Integer.valueOf(parts[2]));
        result.put("senderNickname", parts[4]);
        result.put("senderFlag", parts[5]);
        result.put("subscriptionMonth", parts[6]);
        return result;
    }
}