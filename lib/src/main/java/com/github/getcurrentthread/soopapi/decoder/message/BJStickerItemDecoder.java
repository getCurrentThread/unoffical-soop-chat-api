package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class BJStickerItemDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        if (parts.length > 0) {
            result.put("type", Integer.parseInt(parts[0]));
        }
        return result;
    }
}