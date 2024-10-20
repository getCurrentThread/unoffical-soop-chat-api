package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class QuitChannelDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("quitType", Integer.parseInt(parts[2]));
        result.put("adminKickCount", Integer.parseInt(parts[3]));
        result.put("nickname", parts[4]);
        result.put("bannedRoomBjId", parts[5]);
        result.put("bannedRoomBjNickname", parts[6]);
        return result;
    }
}