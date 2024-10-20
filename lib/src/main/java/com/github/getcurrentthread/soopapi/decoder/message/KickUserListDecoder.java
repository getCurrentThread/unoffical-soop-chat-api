package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KickUserListDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> kickedUsers = new ArrayList<>();
        for (int i = 0; i < parts.length; i += 6) {
            Map<String, Object> user = new HashMap<>();
            user.put("userId", parts[i]);
            user.put("userNickname", parts[i + 1]);
            user.put("time", parts[i + 2]);
            user.put("orderUserId", parts[i + 3]);
            user.put("orderUserNickname", parts[i + 4]);
            user.put("orderUserFlag", parts[i + 5]);
            kickedUsers.add(user);
        }
        result.put("kickedUsers", kickedUsers);
        return result;
    }
}