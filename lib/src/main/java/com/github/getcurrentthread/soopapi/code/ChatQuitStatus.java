package com.github.getcurrentthread.soopapi.code;

import java.util.HashMap;
import java.util.Map;

/**
 * 채널 퇴장(QUIT_CHANNEL) 사유 코드표.
 *
 * <p>{@code SVC_QUITCH} 패킷이 전달하는 {@code quitType} 정수를 의미 있는 상수로 매핑합니다.
 *
 * @see com.github.getcurrentthread.soopapi.event.model.QuitChannelEvent
 */
public enum ChatQuitStatus {

    /** 연결 종료. */
    CLOSED(0, "Connection Closed"),
    /** 정상 퇴장. */
    NORMAL(1, "Normal Leave"),
    /** 강제 퇴장(kick). 공식 코드표의 철자 {@code KCIK}를 그대로 따릅니다. */
    KCIK(2, "Kicked"),
    /** 채팅 금지(채금). */
    DUMB(3, "Muted"),
    /** 도배 차단. */
    OVERCHAT(4, "Over Chat"),
    /** 블라인드. */
    BLIND(5, "Blind"),
    /** 운영자 강제 퇴장. */
    ADMKICK(6, "Admin Kick"),

    /** 알 수 없는 코드를 위한 센티넬. */
    UNKNOWN(-1, "Unknown");

    private static final Map<Integer, ChatQuitStatus> CODE_MAP;

    static {
        var map = new HashMap<Integer, ChatQuitStatus>();
        for (ChatQuitStatus s : values()) {
            map.put(s.code, s);
        }
        CODE_MAP = Map.copyOf(map);
    }

    private final int code;
    private final String description;

    ChatQuitStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 정수 코드를 {@link ChatQuitStatus}로 매핑합니다. 알 수 없는 코드는 {@link #UNKNOWN}을 반환합니다.
     *
     * @param code 퇴장 사유 코드
     * @return 매핑된 상수 또는 {@link #UNKNOWN}
     */
    public static ChatQuitStatus fromCode(int code) {
        return CODE_MAP.getOrDefault(code, UNKNOWN);
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
