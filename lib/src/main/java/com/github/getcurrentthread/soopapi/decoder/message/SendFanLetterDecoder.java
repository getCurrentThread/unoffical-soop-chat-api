package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class SendFanLetterDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("bjId", parts[0]);
        result.put("bjNickname", parts[1]);
        result.put("senderId", parts[2]);
        result.put("senderNickname", parts[3]);
        result.put("type", Integer.parseInt(parts[5]));
        result.put("count", Integer.parseInt(parts[7]));
        result.put("supporterOrder", parts[8]);
        return result;
    }
}