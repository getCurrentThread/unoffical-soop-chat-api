package com.github.getcurrentthread.afreecatvapi.decoder;

import java.util.HashMap;
import java.util.Map;

public class GiftSubscriptionDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("senderId", parts[1]);
        result.put("senderNickname", parts[2]);
        result.put("receiverId", parts[3]);
        result.put("receiverNickname", parts[4]);
        result.put("subscriptionId", parts[5]);
        result.put("subscriptionNickname", parts[6]);
        result.put("itemType", Integer.valueOf(parts[7]));
        result.put("itemCode", parts[8]);
        result.put("isSubscription", Integer.valueOf(parts[9]));
        result.put("subscriptionType", parts[10]);
        result.put("subscriptionPeriod", parts[11]);
        result.put("subscriptionRemain", Integer.valueOf(parts[12]));
        result.put("subscriptionPaycount", Integer.valueOf(parts[13]));
        return result;
    }
}