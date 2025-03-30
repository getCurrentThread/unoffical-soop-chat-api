package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class SetAdminFlagDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("flag", parts[0]);
        return result;
    }
}
