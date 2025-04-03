package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class ChuserExtendDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Map<String, Integer>> userStatus = new HashMap<>();

        // parts[0]는 채팅방 번호 등 다른 정보일 수 있으므로 1부터 시작
        for (int i = 1; i < parts.length; i += 2) {
            if (i + 1 >= parts.length) break;

            String userId = parts[i];
            String statusData = parts[i + 1];

            Map<String, Integer> status = new HashMap<>();

            // & 기준으로 먼저 분리
            String[] statusPairs = statusData.split("&");
            for (String pair : statusPairs) {
                // = 기준으로 키-값 분리
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    status.put(keyValue[0], Integer.parseInt(keyValue[1]));
                }
            }

            userStatus.put(userId, status);
        }

        result.put("userStatus", userStatus);
        return result;
    }
}
