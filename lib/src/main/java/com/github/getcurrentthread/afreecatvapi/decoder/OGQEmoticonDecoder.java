package com.github.getcurrentthread.afreecatvapi.decoder;

import com.github.getcurrentthread.afreecatvapi.decoder.IMessageDecoder;

import java.util.HashMap;
import java.util.Map;

public class OGQEmoticonDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("chatNo", parts[0]);
        result.put("message", parts[1]);
        result.put("groupId", parts[2]);
        result.put("subId", parts[3]);
        result.put("version", parts[4]);
        result.put("senderId", parts[5]);
        result.put("senderNickname", parts[6]);
        result.put("senderFlag", parts[7]);
        result.put("color", parts[8]);
        result.put("chatLang", parts[9]);
        result.put("type", Integer.valueOf(parts[10]));
        result.put("ext", parts[11]);
        result.put("subscriptionMonth", parts[12]);
        return result;
    }
}