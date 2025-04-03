package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class DirectChatDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", parts[0]);
        result.put("senderId", parts[1]);
        result.put("receiverId", parts[2]);
        result.put("type", Integer.parseInt(parts[3]));
        result.put("senderNickname", parts[5]);
        result.put("receiverNickname", parts[6]);
        result.put("flag", parts[7]);
        return result;
    }
}
