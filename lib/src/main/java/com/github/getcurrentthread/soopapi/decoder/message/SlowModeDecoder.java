package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class SlowModeDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("autotime", Integer.parseInt(parts[0]));
        result.put("manualtime", Integer.parseInt(parts[1]));
        return result;
    }
}