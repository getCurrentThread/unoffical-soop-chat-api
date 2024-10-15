package com.github.getcurrentthread.afreecatvapi.model;

public enum MessageType {
    KEEP_ALIVE(0),
    LOGIN(1),
    JOIN_CHANNEL(2),
    QUIT_CHANNEL(3),
    CHAT_USER(4),
    CHAT_MESSAGE(5),
    SET_CHANNEL_NAME(6),
    SET_BJ_STAT(7),
    SET_DUMB(8),
    DIRECT_CHAT(9),
    NOTICE(10),
    KICK(11),
    SET_USER_FLAG(12),
    SET_SUB_BJ(13),
    SET_NICKNAME(14),
    SERVER_STAT(15),
    NULL_16(16),
    CLUB_COLOR(17),
    SEND_BALLOON(18),
    ICE_MODE(19),
    SEND_FAN_LETTER(20),
    ICE_MODE_EX(21),
    GET_ICE_MODE_RELAY(22),
    SLOW_MODE(23),
    RELOAD_BURN_LEVEL(24),
    BLIND_KICK(25),
    MANAGER_CHAT(26),
    APPEND_DATA(27),
    BASEBALL_EVENT(28),
    PAID_ITEM(29),
    TOP_FAN(30),
    SNS_MESSAGE(31),
    SNS_MODE(32),
    SEND_BALLOON_SUB(33),
    SEND_FAN_LETTER_SUB(34),
    TOP_FAN_SUB(35),
    BJ_STICKER_ITEM(36),
    CHOCOLATE(37),
    CHOCOLATE_SUB(38),
    TOP_CLAN(39),
    TOP_CLAN_SUB(40),
    SUPER_CHAT(41),
    UPDATE_TICKET(42),
    NOTI_GAME_RANKER(43),
    STAR_COIN(44),
    SEND_QUICK_VIEW(45),
    ITEM_STATUS(46),
    ITEM_USING(47),
    USE_QUICK_VIEW(48),
    NOTIFY_POLL(50),
    CHAT_BLOCK_MODE(51),
    BDM_ADD_BLACK_INFO(52),
    SET_BROAD_INFO(53),
    BAN_WORD(54),
    SEND_ADMIN_NOTICE(58),
    FREECAT_OWNER_JOIN(65),
    BUY_GOODS(70),
    BUY_GOODS_SUB(71),
    SEND_PROMOTION(72),
    NOTIFY_VR(74),
    NOTIFY_MOBBROAD_PAUSE(75),
    KICK_AND_CANCEL(76),
    KICK_USERLIST(77),
    ADMIN_CHAT_USER(78),
    CLI_DOBAE_INFO(79),
    VOD_BALLOON(86),
    ADCON_EFFECT(87),
    KICK_MSG_STATE(90),
    FOLLOW_ITEM(91),
    ITEM_SELL_EFFECT(92),
    FOLLOW_ITEM_EFFECT(93),
    TRANSLATION_STATE(94),
    TRANSLATION(95),
    GIFT_TICKET(102),
    VOD_ADCON(103),
    BJ_NOTICE(104),
    VIDEO_BALLOON(105),
    STATION_ADCON(107),
    SEND_SUBSCRIPTION(108),
    OGQ_EMOTICON(109),
    ITEM_DROPS(111),
    VIDEO_BALLOON_LINK(117),
    OGQ_EMOTICON_GIFT(118),
    AD_IN_BROAD_JSON(119),
    GEM_ITEM_SEND(120),
    MISSION(121),
    LIVE_CAPTION(122),
    MISSION_SETTLE(125),
    SET_ADMIN_FLAG(126),
    CHUSER_EXTEND(127),
    ADMIN_CHUSER_EXTEND(128),
    NONE_TYPE(-1);

    private final int code;

    MessageType(int code) {
        this.code = code;
    }

    public static MessageType fromCode(int code) {
        for (MessageType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return NONE_TYPE;
    }
}