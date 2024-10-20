package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class FollowItemDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("chatNo", Integer.parseInt(parts[0]));
        result.put("recvId", parts[1]);
        result.put("sendId", parts[2]);
        result.put("sendNick", parts[3]);
        result.put("type", Integer.parseInt(parts[4]));
        return result;
    }
}