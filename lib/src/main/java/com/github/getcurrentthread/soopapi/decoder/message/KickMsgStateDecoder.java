package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class KickMsgStateDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        if (parts.length >= 2) {
            result.put("chatNo", parts[0]);
            result.put("isHideKickMessage", "1".equals(parts[1]));
        }
        return result;
    }
}