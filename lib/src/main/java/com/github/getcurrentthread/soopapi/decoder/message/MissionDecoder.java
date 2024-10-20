package com.github.getcurrentthread.soopapi.decoder.message;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class MissionDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
//        try {
        Map<String, Object> missionData = new Gson().fromJson(parts[0], Map.class);
        String type = (String) missionData.get("type");
        result.put("type", type);

        switch (type) {
            case "GIFT":
            case "CHALLENGE_GIFT":
                result.put("userId", missionData.get("user_id"));
                result.put("isRelay", missionData.get("is_relay"));
                result.put("userNick", missionData.get("user_nick"));
                result.put("giftCnt", missionData.get("gift_count"));
                result.put("image", missionData.get("image"));
                break;
            case "SETTLE":
            case "CHALLENGE_SETTLE":
                result.put("settleCnt", missionData.get("settle_count"));
                result.put("image", missionData.get("image"));
                break;
            case "NOTICE":
                result.put("draw", missionData.get("draw"));
                result.put("winner", missionData.get("winner"));
                break;
            case "CHALLENGE_NOTICE":
                result.put("missionStatus", missionData.get("mission_status"));
                result.put("title", missionData.get("title"));
                break;
        }
//        } catch (JsonSyntaxException e) {
//            // Handle JSON parsing error
//        }
        return result;
    }
}