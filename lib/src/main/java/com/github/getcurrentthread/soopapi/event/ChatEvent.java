package com.github.getcurrentthread.soopapi.event;

import java.util.HashMap;
import java.util.Map;

public enum ChatEvent {

    // ── 기본 연결 (0~2) ──
    KEEP_ALIVE(0, "연결 유지"), // 핑퐁
    LOGIN(1, "로그인 핸드쉐이크"),
    JOIN_CHANNEL(2, "채널 입장 핸드쉐이크"),

    // ── 채팅 / 사용자 (3~9) ──
    QUIT_CHANNEL(3, "채널 퇴장"), // 강제 퇴장
    CHAT_USER(4, "채팅 사용자 입장/퇴장"),
    CHAT_MESSAGE(5, "채팅 메시지"),
    SET_CHANNEL_NAME(6, "채널 이름 설정"),
    SET_BJ_STAT(7, "BJ 상태 설정"), // 방송 연결 해제 시 수신
    SET_DUMB(8, "채팅 금지"), // 채금
    DIRECT_CHAT(9, "1:1 채팅"),

    // ── 공지 / 관리 (10~17) ──
    NOTICE(10, "공지사항"), // 미사용
    KICK(11, "강제 퇴장"), // 미사용
    SET_USER_FLAG(12, "사용자 플래그 설정"), // 입장 정보 변경
    SET_SUB_BJ(13, "서브 BJ 설정"),
    SET_NICKNAME(14, "닉네임 설정"),
    SERVER_STAT(15, "서버 상태"), // 미사용
    NULL_16(16, "미사용"),
    CLUB_COLOR(17, "클럽 색상"),

    // ── 후원 / 별풍선 (18~22) ──
    SEND_BALLOON(18, "별풍선 전송"),
    ICE_MODE(19, "얼음방 모드"),
    SEND_FAN_LETTER(20, "팬레터 전송"),
    ICE_MODE_EX(21, "확장 얼음방 모드"), // 로그상 21→19 순서
    GET_ICE_MODE_RELAY(22, "얼음방 모드 릴레이 받기"), // 미사용

    // ── 채팅 제어 (23~27) ──
    SLOW_MODE(23, "슬로우 모드"),
    RELOAD_BURN_LEVEL(24, "번 레벨 리로드"), // 미사용
    BLIND_KICK(25, "블라인드 강퇴"), // 미사용
    MANAGER_CHAT(26, "매니저 채팅"), // 매니저 flag 이상만 수신 가능
    APPEND_DATA(27, "데이터 추가"), // 미사용

    // ── 이벤트 / 아이템 (28~48) ──
    BASEBALL_EVENT(28, "야구 이벤트"), // 미사용
    PAID_ITEM(29, "유료 아이템"), // 미사용
    TOP_FAN(30, "열혈팬"), // 미사용
    SNS_MESSAGE(31, "SNS 메시지"), // 미사용
    SNS_MODE(32, "SNS 모드"), // 미사용
    SEND_BALLOON_SUB(33, "별풍선 전송 (서브)"),
    SEND_FAN_LETTER_SUB(34, "팬레터 전송 (서브)"),
    TOP_FAN_SUB(35, "톱 팬 (서브)"), // 미사용
    BJ_STICKER_ITEM(36, "BJ 스티커 아이템"), // 미사용
    CHOCOLATE(37, "초콜릿"),
    CHOCOLATE_SUB(38, "초콜릿 (서브)"),
    TOP_CLAN(39, "톱 클랜"), // 미사용
    TOP_CLAN_SUB(40, "톱 클랜 (서브)"), // 미사용
    SUPER_CHAT(41, "슈퍼 채팅"), // 미사용
    UPDATE_TICKET(42, "티켓 업데이트"), // 미사용
    NOTI_GAME_RANKER(43, "게임 랭커 알림"), // 미사용
    STAR_COIN(44, "스타 코인"),
    SEND_QUICK_VIEW(45, "퀵뷰 선물"),
    ITEM_STATUS(46, "아이템 상태"), // 미사용
    ITEM_USING(47, "아이템 사용 중"),
    USE_QUICK_VIEW(48, "퀵뷰 사용"),

    // ── 투표 / 차단 / 방송 정보 (50~58) ──
    NOTIFY_POLL(50, "투표 알림"),
    CHAT_BLOCK_MODE(51, "채팅 차단 모드"), // 미사용
    BDM_ADD_BLACK_INFO(52, "블랙리스트 정보 추가"),
    SET_BROAD_INFO(53, "방송 정보 설정"), // 미사용
    BAN_WORD(54, "금지어 설정"),
    SEND_ADMIN_NOTICE(58, "관리자 공지 전송"),

    // ── 프리캣 / 상품 / 프로모션 (65~75) ──
    FREECAT_OWNER_JOIN(65, "프리캣 소유자 입장"),
    BUY_GOODS(70, "상품 구매"),
    BUY_GOODS_SUB(71, "상품 구매 (서브)"),
    SEND_PROMOTION(72, "프로모션 전송"), // 미사용
    NOTIFY_VR(74, "VR 알림"),
    NOTIFY_MOBBROAD_PAUSE(75, "모바일 방송 일시정지 알림"),

    // ── 강퇴 / 관리자 (76~79) ──
    KICK_AND_CANCEL(76, "강퇴 및 취소"),
    KICK_USERLIST(77, "강퇴 사용자 목록"),
    ADMIN_CHAT_USER(78, "관리자 채팅 사용자"),
    CLI_DOBAE_INFO(79, "도배 정보"),

    // ── 후원 / 애드콘 (86~87) ──
    VOD_BALLOON(86, "VOD 풍선"),
    ADCON_EFFECT(87, "애드콘 효과"), // 애드벌룬 후원

    // ── 구독 / 번역 (90~95) ──
    KICK_MSG_STATE(90, "강퇴 메시지 상태"),
    FOLLOW_ITEM(91, "신규 구독"),
    ITEM_SELL_EFFECT(92, "아이템 판매 효과"),
    FOLLOW_ITEM_EFFECT(93, "연속 구독"),
    TRANSLATION_STATE(94, "번역 상태"),
    TRANSLATION(95, "번역"),

    // ── 티켓 / 공지 / 영상 후원 (102~109) ──
    GIFT_TICKET(102, "선물 티켓"),
    VOD_ADCON(103, "VOD 애드콘"),
    BJ_NOTICE(104, "BJ 공지"),
    VIDEO_BALLOON(105, "영상 후원"),
    STATION_ADCON(107, "스테이션 애드콘"),
    SEND_SUBSCRIPTION(108, "구독권 선물"),
    OGQ_EMOTICON(109, "OGQ 이모티콘"),

    // ── 아이템 / 이모티콘 / 광고 (110~122) ──
    EMOTICON_TICKET(110, "이모티콘 티켓"), // 채널 입장 직후 수신, value=1
    ITEM_DROPS(111, "아이템 드롭"),
    VIDEO_BALLOON_LINK(117, "비디오 풍선 링크"), // 미사용
    OGQ_EMOTICON_GIFT(118, "OGQ 이모티콘 선물"),
    AD_IN_BROAD_JSON(119, "방송 내 광고 JSON"),
    GEM_ITEM_SEND(120, "젬 아이템 전송"),
    MISSION(121, "도전 미션"),
    LIVE_CAPTION(122, "실시간 자막"),

    // ── 미션 / 관리자 / 사용자 확장 (125~128) ──
    MISSION_SETTLE(125, "미션 정산"),
    SET_ADMIN_FLAG(126, "관리자 플래그 설정"),
    CHUSER_EXTEND(127, "구독자 리스트"),
    ADMIN_CHUSER_EXTEND(128, "관리자 채팅 사용자 확장"),

    // ── 알 수 없는 타입 ──
    NONE_TYPE(-1, "알 수 없는 타입");

    private static final StableValue<Map<Integer, ChatEvent>> CODE_MAP = StableValue.of();

    private final int code;
    private final String description;

    ChatEvent(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ChatEvent fromCode(int code) {
        return CODE_MAP.orElseSet(
                        () -> {
                            var map = new HashMap<Integer, ChatEvent>();
                            for (ChatEvent e : values()) {
                                map.put(e.code, e);
                            }
                            return Map.copyOf(map);
                        })
                .getOrDefault(code, NONE_TYPE);
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
