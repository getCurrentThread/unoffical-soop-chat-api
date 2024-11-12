package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class SetDumbDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("userId", parts[0]);          // 음소거 대상 유저 ID
        result.put("userInfo", parts[1]);        // 사용자 플래그 정보
        result.put("dumbTime", Integer.parseInt(parts[2]));    // 음소거 시간
        result.put("dumbCount", Integer.parseInt(parts[3]));   // 음소거 횟수
        result.put("adminId", parts[4]);        // 관리자 ID
        result.put("adminType", Integer.parseInt(parts[5]));   // 관리자 타입 (1: 스트리머, 2: 매니저 등)
        result.put("extraInfo", parts[6]);         // extraInfo
        result.put("userNickname", parts[7]);   // 관리자 닉네임

        return result;
    }
}