package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class NotifyVRDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", Integer.parseInt(parts[0]));
        result.put("bjId", parts[1]);
        result.put("vrId", parts[2]);
        result.put("rtmp", parts[3]);
        result.put("hls", parts[4]);
        result.put("vrType", Integer.parseInt(parts[5]));
        return result;
    }
}