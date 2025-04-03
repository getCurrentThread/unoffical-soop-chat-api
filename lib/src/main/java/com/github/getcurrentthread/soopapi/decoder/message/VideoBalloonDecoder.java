package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class VideoBalloonDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("chatNo", parts[0]);
        result.put("bjId", parts[1]);
        result.put("userId", parts[2]);
        result.put("userNickname", parts[3]);
        result.put("balloonCount", Integer.parseInt(parts[4]));
        result.put("fanOrder", Integer.parseInt(parts[5]));
        result.put("isTopFan", Integer.parseInt(parts[7]));
        result.put("relay", parts[8]);
        result.put("fileName", parts[12]);
        result.put("isDefault", "1".equals(parts[13]));

        if (parts.length > 14) {
            result.put("extraData", parts[14]);
        }
        return result;
    }
}
