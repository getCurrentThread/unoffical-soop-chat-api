package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class TranslationDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("idx", Integer.parseInt(parts[0]));
        result.put("mode", Integer.parseInt(parts[1]));
        result.put("message", parts[2]);
        result.put("orgLanguage", Integer.parseInt(parts[3]));
        result.put("transLanguage", Integer.parseInt(parts[4]));
        return result;
    }
}
