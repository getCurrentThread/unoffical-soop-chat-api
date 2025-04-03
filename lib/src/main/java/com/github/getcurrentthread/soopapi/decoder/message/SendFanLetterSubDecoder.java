package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class SendFanLetterSubDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("bjId", parts[1]);
        result.put("bjNickname", parts[2]);
        result.put("senderId", parts[3]);
        result.put("senderNickname", parts[4]);
        result.put("type", Integer.parseInt(parts[6]));
        result.put("count", Integer.parseInt(parts[8]));
        result.put("supporterOrder", parts[9]);
        return result;
    }
}
