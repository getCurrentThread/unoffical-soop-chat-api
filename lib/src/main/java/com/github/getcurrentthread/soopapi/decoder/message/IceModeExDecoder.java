package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class IceModeExDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("iceMode", Integer.parseInt(parts[0]));
        result.put("freezeType", Integer.parseInt(parts[2]));
        result.put("balloonLimitCount", Integer.parseInt(parts[3]));
        result.put("subscriptionLimitCount", Integer.parseInt(parts[4]));
        return result;
    }
}