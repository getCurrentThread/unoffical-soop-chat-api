package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class ChatMessageDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", parts[0]);
        result.put("senderId", parts[1]);
        result.put("type", Integer.parseInt(parts[3]));
        result.put("chatLang", Integer.parseInt(parts[4]));
        result.put("senderNickname", parts[5]);
        result.put("senderFlag", parts[6]);
        result.put("subscriptionMonth", parts[7]);
        result.put("randomNicknameColor", parts.length > 8 ? parts[8] : "");
        result.put("randomNicknameColorDarkmode", parts.length > 9 ? parts[9] : "");
        return result;
    }
}