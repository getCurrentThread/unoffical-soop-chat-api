package com.github.getcurrentthread.soopapi.websocket;

import java.util.StringJoiner;

import com.github.getcurrentthread.soopapi.constant.SOOPConstants;
import com.github.getcurrentthread.soopapi.model.ChannelInfo;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class WebSocketPacketBuilder {
    private static final String PING_PACKET = buildPacket("0000", SOOPConstants.F);

    // 주기적 연결 유지를 위한 핑 패킷 생성
    public static String createPingPacket() {
        return PING_PACKET;
    }

    // 초기 연결을 위한 패킷 생성 (익명)
    public static String createConnectPacket() {
        return createConnectPacket(null);
    }

    // 초기 연결을 위한 패킷 생성 (인증 지원)
    public static String createConnectPacket(String authTicket) {
        String payload;
        if (authTicket != null && !authTicket.isEmpty()) {
            payload =
                    SOOPConstants.F
                            + authTicket
                            + SOOPConstants.F.repeat(2)
                            + "16"
                            + SOOPConstants.F;
        } else {
            payload = SOOPConstants.F.repeat(3) + "16" + SOOPConstants.F;
        }
        return buildPacket("0001", payload);
    }

    // 채팅방 입장을 위한 패킷 생성 (익명)
    public static String createJoinPacket(ChannelInfo channelInfo) {
        return createJoinPacket(channelInfo, null, null);
    }

    // 채팅방 입장을 위한 패킷 생성 (인증 지원)
    public static String createJoinPacket(ChannelInfo channelInfo, String authTicket, String uuid) {
        StringBuilder payload = new StringBuilder();
        payload.append(SOOPConstants.F).append(channelInfo.CHATNO());

        if (authTicket != null && !authTicket.isEmpty()) {
            payload.append(SOOPConstants.F).append(channelInfo.FTK());
            payload.append(SOOPConstants.F).append("0");
            payload.append(SOOPConstants.F);

            // log 메타데이터 블록
            String logQuery = buildLogQuery(channelInfo, uuid);
            payload.append("log")
                    .append(SOOPConstants.ELEMENT_START)
                    .append(logQuery)
                    .append(SOOPConstants.ELEMENT_END);

            payload.append("pwd")
                    .append(SOOPConstants.ELEMENT_START)
                    .append(SOOPConstants.ELEMENT_END);

            payload.append("auth_info")
                    .append(SOOPConstants.ELEMENT_START)
                    .append("NULL")
                    .append(SOOPConstants.ELEMENT_END);

            payload.append("pver")
                    .append(SOOPConstants.ELEMENT_START)
                    .append("2")
                    .append(SOOPConstants.ELEMENT_END);

            payload.append("access_system")
                    .append(SOOPConstants.ELEMENT_START)
                    .append("html5")
                    .append(SOOPConstants.ELEMENT_END);

            payload.append(SOOPConstants.F);
        } else {
            payload.append(SOOPConstants.F.repeat(5));
        }

        return buildPacket("0002", payload.toString());
    }

    // ENTER_INFO 패킷 생성 (인증된 사용자 전용)
    public static String createEnterInfoPacket(String synAck) {
        String payload = SOOPConstants.F + synAck + SOOPConstants.F + "0" + SOOPConstants.F;
        return buildPacket("0012", payload);
    }

    // 채팅 메시지 전송을 위한 패킷 생성
    public static String createChatPacket(String message) {
        return buildPacket("0005", SOOPConstants.F + message + SOOPConstants.F.repeat(6));
    }

    // 패킷 구조 생성을 위한 유틸리티 메서드
    private static String buildPacket(String command, String data) {
        int byteLength = SOOPChatUtils.utf8ByteLength(data);
        String lengthStr = String.valueOf(byteLength);
        StringBuilder sb =
                new StringBuilder(SOOPConstants.ESC.length() + 4 + 6 + 2 + data.length());
        sb.append(SOOPConstants.ESC);
        sb.append(command);
        for (int i = lengthStr.length(); i < 6; i++) {
            sb.append('0');
        }
        sb.append(lengthStr);
        sb.append("00");
        sb.append(data);
        return sb.toString();
    }

    // log 메타데이터 쿼리 문자열 생성
    private static String buildLogQuery(ChannelInfo channelInfo, String uuid) {
        StringJoiner joiner = new StringJoiner("");
        appendParam(joiner, "set_bps", channelInfo.BPS());
        appendParam(joiner, "view_bps", channelInfo.BPS());
        appendParam(joiner, "quality", "normal");
        appendParam(joiner, "uuid", uuid != null ? uuid : "");
        appendParam(joiner, "geo_cc", channelInfo.geoCC());
        appendParam(joiner, "geo_rc", channelInfo.geoRC());
        appendParam(joiner, "acpt_lang", channelInfo.acptLang());
        appendParam(joiner, "svc_lang", channelInfo.svcLang());
        appendParam(joiner, "subscribe", "0");
        appendParam(joiner, "lowlatency", "0");
        appendParam(joiner, "mode", "landing");
        return joiner.toString();
    }

    private static void appendParam(StringJoiner joiner, String key, String value) {
        joiner.add(
                SOOPConstants.SPACE
                        + "&"
                        + SOOPConstants.SPACE
                        + key
                        + SOOPConstants.SPACE
                        + "="
                        + SOOPConstants.SPACE
                        + (value != null ? value : ""));
    }

    // 패킷 길이 계산을 위한 유틸리티 메서드
    public static int calculateByteSize(String data) {
        return SOOPChatUtils.utf8ByteLength(data) + 6;
    }
}
