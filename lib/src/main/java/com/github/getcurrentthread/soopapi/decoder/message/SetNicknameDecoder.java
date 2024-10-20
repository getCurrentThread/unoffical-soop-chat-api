package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class SetNicknameDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("userId", parts[0]);
        result.put("newNickname", parts[1]);
        result.put("changeType", Integer.parseInt(parts[2]));
        result.put("flag", parts[3]);
        result.put("oldNickname", parts[4]);
        return result;
    }
}
