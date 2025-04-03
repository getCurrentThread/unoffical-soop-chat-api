package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatUserDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", Integer.valueOf(parts[0]));
        List<Map<String, Object>> userList = new ArrayList<>();

        for (int i = 1; i < parts.length; i += 3) {
            if (i + 2 < parts.length) {
                Map<String, Object> user = new HashMap<>();
                user.put("id", parts[i]);
                user.put("nickname", parts[i + 1]);
                user.put("flag", parts[i + 2]);
                userList.add(user);
            }
        }

        result.put("userList", userList);
        return result;
    }
}
