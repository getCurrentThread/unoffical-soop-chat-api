package com.github.getcurrentthread.soopapi.model;

public enum MessageType {
    KEEP_ALIVE(0, "연결 유지"),
    LOGIN(1, "로그인"),
    JOIN_CHANNEL(2, "채널 입장"),
    QUIT_CHANNEL(3, "채널 퇴장"),
    CHAT_USER(4, "채팅 사용자"),
    CHAT_MESSAGE(5, "채팅 메시지"),
    SET_CHANNEL_NAME(6, "채널 이름 설정"),
    SET_BJ_STAT(7, "BJ 상태 설정"),
    SET_DUMB(8, "음소거 설정"),
    DIRECT_CHAT(9, "1:1 채팅"),
    NOTICE(10, "공지사항"),
    KICK(11, "강제 퇴장"),
    SET_USER_FLAG(12, "사용자 플래그 설정"),
    SET_SUB_BJ(13, "서브 BJ 설정"),
    SET_NICKNAME(14, "닉네임 설정"),
    SERVER_STAT(15, "서버 상태"),
    NULL_16(16, "미사용"),
    CLUB_COLOR(17, "클럽 색상"),
    SEND_BALLOON(18, "풍선 전송"),
    ICE_MODE(19, "얼음방 모드"),
    SEND_FAN_LETTER(20, "팬레터 전송"),
    ICE_MODE_EX(21, "확장 얼음방 모드"),
    GET_ICE_MODE_RELAY(22, "얼음방 모드 릴레이 받기"),
    SLOW_MODE(23, "슬로우 모드"),
    RELOAD_BURN_LEVEL(24, "번 레벨 리로드"),
    BLIND_KICK(25, "블라인드 강퇴"),
    MANAGER_CHAT(26, "매니저 채팅"),
    APPEND_DATA(27, "데이터 추가"),
    BASEBALL_EVENT(28, "야구 이벤트"),
    PAID_ITEM(29, "유료 아이템"),
    TOP_FAN(30, "톱 팬"),
    SNS_MESSAGE(31, "SNS 메시지"),
    SNS_MODE(32, "SNS 모드"),
    SEND_BALLOON_SUB(33, "풍선 전송 (서브)"),
    SEND_FAN_LETTER_SUB(34, "팬레터 전송 (서브)"),
    TOP_FAN_SUB(35, "톱 팬 (서브)"),
    BJ_STICKER_ITEM(36, "BJ 스티커 아이템"),
    CHOCOLATE(37, "초콜릿"),
    CHOCOLATE_SUB(38, "초콜릿 (서브)"),
    TOP_CLAN(39, "톱 클랜"),
    TOP_CLAN_SUB(40, "톱 클랜 (서브)"),
    SUPER_CHAT(41, "슈퍼 채팅"),
    UPDATE_TICKET(42, "티켓 업데이트"),
    NOTI_GAME_RANKER(43, "게임 랭커 알림"),
    STAR_COIN(44, "스타 코인"),
    SEND_QUICK_VIEW(45, "퀵뷰 전송"),
    ITEM_STATUS(46, "아이템 상태"),
    ITEM_USING(47, "아이템 사용 중"),
    USE_QUICK_VIEW(48, "퀵뷰 사용"),
    NOTIFY_POLL(50, "투표 알림"),
    CHAT_BLOCK_MODE(51, "채팅 차단 모드"),
    BDM_ADD_BLACK_INFO(52, "블랙리스트 정보 추가"),
    SET_BROAD_INFO(53, "방송 정보 설정"),
    BAN_WORD(54, "금지어 설정"),
    SEND_ADMIN_NOTICE(58, "관리자 공지 전송"),
    FREECAT_OWNER_JOIN(65, "프리캣 소유자 입장"),
    BUY_GOODS(70, "상품 구매"),
    BUY_GOODS_SUB(71, "상품 구매 (서브)"),
    SEND_PROMOTION(72, "프로모션 전송"),
    NOTIFY_VR(74, "VR 알림"),
    NOTIFY_MOBBROAD_PAUSE(75, "모바일 방송 일시정지 알림"),
    KICK_AND_CANCEL(76, "강퇴 및 취소"),
    KICK_USERLIST(77, "강퇴 사용자 목록"),
    ADMIN_CHAT_USER(78, "관리자 채팅 사용자"),
    CLI_DOBAE_INFO(79, "도배 정보"),
    VOD_BALLOON(86, "VOD 풍선"),
    ADCON_EFFECT(87, "애드콘 효과"),
    KICK_MSG_STATE(90, "강퇴 메시지 상태"),
    FOLLOW_ITEM(91, "팔로우(구독) 아이템"),
    ITEM_SELL_EFFECT(92, "아이템 판매 효과"),
    FOLLOW_ITEM_EFFECT(93, "팔로우(구독) 아이템 효과"),
    TRANSLATION_STATE(94, "번역 상태"),
    TRANSLATION(95, "번역"),
    GIFT_TICKET(102, "선물 티켓"),
    VOD_ADCON(103, "VOD 애드콘"),
    BJ_NOTICE(104, "BJ 공지"),
    VIDEO_BALLOON(105, "비디오 풍선"),
    STATION_ADCON(107, "스테이션 애드콘"),
    SEND_SUBSCRIPTION(108, "구독 전송"),
    OGQ_EMOTICON(109, "OGQ 이모티콘"),
    ITEM_DROPS(111, "아이템 드롭"),
    VIDEO_BALLOON_LINK(117, "비디오 풍선 링크"),
    OGQ_EMOTICON_GIFT(118, "OGQ 이모티콘 선물"),
    AD_IN_BROAD_JSON(119, "방송 내 광고 JSON"),
    GEM_ITEM_SEND(120, "젬 아이템 전송"),
    MISSION(121, "미션"),
    LIVE_CAPTION(122, "실시간 자막"),
    MISSION_SETTLE(125, "미션 정산"),
    SET_ADMIN_FLAG(126, "관리자 플래그 설정"),
    CHUSER_EXTEND(127, "채팅 사용자 확장"),
    ADMIN_CHUSER_EXTEND(128, "관리자 채팅 사용자 확장"),
    NONE_TYPE(-1, "알 수 없는 타입");

    private final int code;
    private final String description;

    MessageType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static MessageType fromCode(int code) {
        for (MessageType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return NONE_TYPE;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
