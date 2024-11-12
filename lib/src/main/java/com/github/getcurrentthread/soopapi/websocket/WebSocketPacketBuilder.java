package com.github.getcurrentthread.soopapi.websocket;

import com.github.getcurrentthread.soopapi.constant.SOOPConstants;
import com.github.getcurrentthread.soopapi.model.ChannelInfo;

public class WebSocketPacketBuilder {
    // 주기적 연결 유지를 위한 핑 패킷 생성
    public static String createPingPacket() {
        return buildPacket("0000", SOOPConstants.F);
    }

    // 초기 연결을 위한 패킷 생성 
    public static String createConnectPacket() {
        return buildPacket("0001", String.format("%s16%s", SOOPConstants.F.repeat(3), SOOPConstants.F));
    }

    // 채팅방 입장을 위한 패킷 생성
    public static String createJoinPacket(ChannelInfo channelInfo) {
        return buildPacket("0002", String.format("%s%s%s", 
            SOOPConstants.F,
            channelInfo.CHATNO,
            SOOPConstants.F.repeat(5)));
    }

    // 패킷 구조 생성을 위한 유틸리티 메서드
    private static String buildPacket(String command, String data) {
        return String.format("%s%s%06d00%s", 
            SOOPConstants.ESC,
            command, 
            data.length(),
            data);
    }

    // 패킷 길이 계산을 위한 유틸리티 메서드  
    public static int calculateByteSize(String data) {
        return data.getBytes().length + 6;
    }
}