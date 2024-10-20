package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class ChuserExtendDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Integer> userSubscriptionMonth = new HashMap<>();
        for (int i = 0; i < parts.length; i += 2) {
            String userId = parts[i];
            int subscriptionMonth = Integer.parseInt(parts[i + 1].split("=")[1]);
            userSubscriptionMonth.put(userId, subscriptionMonth);
        }
        result.put("userSubscriptionMonth", userSubscriptionMonth);
        return result;
    }
}