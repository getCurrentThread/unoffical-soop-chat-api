package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class AdminChatUserDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        if ("1".equals(parts[0])) {
            result.put("type", "list");
            result.put("employees", new HashMap<>());
            result.put("admins", new HashMap<>());
            result.put("managers", new HashMap<>());
            for (int i = 1; i < parts.length && parts[i] != null && !parts[i].isEmpty(); i += 3) {
                Map<String, Object> user = new HashMap<>();
                user.put("id", parts[i]);
                user.put("nickname", parts[i + 1]);
                user.put("flag", parts[i + 2]);
            }
        }
        return result;
    }
}
