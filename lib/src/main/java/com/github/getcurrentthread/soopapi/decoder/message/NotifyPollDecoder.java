package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class NotifyPollDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", Integer.parseInt(parts[0]));
        result.put("bjId", parts[1]);
        result.put("no", Integer.parseInt(parts[2]));
        result.put("show", Integer.parseInt(parts[3]));
        return result;
    }
}
