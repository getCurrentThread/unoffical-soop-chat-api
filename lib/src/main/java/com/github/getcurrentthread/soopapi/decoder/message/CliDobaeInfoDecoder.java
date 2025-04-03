package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class CliDobaeInfoDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("dobaeInfo", Integer.parseInt(parts[0]));
        result.put("userId", parts[1]);
        return result;
    }
}
