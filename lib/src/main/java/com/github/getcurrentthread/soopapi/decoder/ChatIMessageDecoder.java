package com.github.getcurrentthread.soopapi.decoder;

import java.util.HashMap;
import java.util.Map;

public class ChatIMessageDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("senderId", parts[2].split("[(]")[0].trim());
        result.put("comment", parts[1]);
        result.put("senderNickname", parts[6]);
        return result;
    }
}