package com.github.getcurrentthread.soopapi.decoder.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.getcurrentthread.soopapi.decoder.message.*;
import com.github.getcurrentthread.soopapi.model.MessageType;

public class DefaultMessageDecoderFactory implements MessageDecoderFactory {
    @Override
    public Map<MessageType, IMessageDecoder> createDecoders() {
        return new ConcurrentHashMap<>(
                Map.<MessageType, IMessageDecoder>ofEntries(
                        Map.entry(MessageType.CHAT_MESSAGE, new ChatMessageDecoder()),
                        Map.entry(MessageType.SEND_BALLOON, new SendBalloonDecoder()),
                        Map.entry(MessageType.OGQ_EMOTICON, new OGQEmoticonDecoder()),
                        Map.entry(MessageType.OGQ_EMOTICON_GIFT, new GiftOGQEmoticonDecoder()),
                        Map.entry(MessageType.MANAGER_CHAT, new ManagerChatDecoder()),
                        Map.entry(MessageType.CHOCOLATE, new ChocolateDecoder()),
                        Map.entry(MessageType.CHOCOLATE_SUB, new ChocolateDecoder()),
                        Map.entry(MessageType.SEND_QUICK_VIEW, new QuickViewDecoder()),
                        Map.entry(MessageType.GIFT_TICKET, new GiftTicketDecoder()),
                        Map.entry(MessageType.VOD_BALLOON, new VODBalloonDecoder()),
                        Map.entry(MessageType.ADCON_EFFECT, new AdconEffectDecoder()),
                        Map.entry(MessageType.VIDEO_BALLOON, new VideoBalloonDecoder()),
                        Map.entry(MessageType.SEND_SUBSCRIPTION, new GiftSubscriptionDecoder()),
                        Map.entry(MessageType.ITEM_DROPS, new ItemDropsDecoder()),
                        Map.entry(MessageType.GEM_ITEM_SEND, new GemItemDecoder()),
                        Map.entry(MessageType.LIVE_CAPTION, new LiveCaptionDecoder()),
                        Map.entry(MessageType.SLOW_MODE, new SlowModeDecoder()),
                        Map.entry(MessageType.SET_ADMIN_FLAG, new SetAdminFlagDecoder()),
                        Map.entry(MessageType.TOP_FAN, new TopFanDecoder()),
                        Map.entry(MessageType.TOP_FAN_SUB, new TopFanSubDecoder()),
                        Map.entry(MessageType.SUPER_CHAT, new SuperChatDecoder()),
                        Map.entry(MessageType.STAR_COIN, new StarCoinDecoder()),
                        Map.entry(MessageType.ADMIN_CHAT_USER, new AdminChatUserDecoder()),
                        Map.entry(MessageType.ITEM_STATUS, new ItemStatusDecoder()),
                        Map.entry(MessageType.ITEM_USING, new ItemUsingDecoder()),
                        Map.entry(MessageType.USE_QUICK_VIEW, new UseQuickViewDecoder()),
                        Map.entry(MessageType.NOTIFY_POLL, new NotifyPollDecoder()),
                        Map.entry(MessageType.CHAT_BLOCK_MODE, new ChatBlockModeDecoder()),
                        Map.entry(MessageType.SET_BROAD_INFO, new SetBroadInfoDecoder()),
                        Map.entry(MessageType.BUY_GOODS, new BuyGoodsDecoder()),
                        Map.entry(MessageType.BUY_GOODS_SUB, new BuyGoodsSubDecoder()),
                        Map.entry(MessageType.SEND_PROMOTION, new SendPromotionDecoder()),
                        Map.entry(MessageType.NOTIFY_VR, new NotifyVRDecoder()),
                        Map.entry(
                                MessageType.NOTIFY_MOBBROAD_PAUSE,
                                new NotifyMobBroadPauseDecoder()),
                        Map.entry(MessageType.FOLLOW_ITEM, new FollowItemDecoder()),
                        Map.entry(MessageType.FOLLOW_ITEM_EFFECT, new FollowItemEffectDecoder()),
                        Map.entry(MessageType.BJ_NOTICE, new BJNoticeDecoder()),
                        Map.entry(MessageType.VIDEO_BALLOON_LINK, new VideoBalloonLinkDecoder()),
                        Map.entry(MessageType.AD_IN_BROAD_JSON, new AdInBroadJsonDecoder()),
                        Map.entry(MessageType.MISSION, new MissionDecoder()),
                        Map.entry(MessageType.MISSION_SETTLE, new MissionSettleDecoder()),
                        Map.entry(MessageType.CHUSER_EXTEND, new ChuserExtendDecoder()),
                        Map.entry(MessageType.ADMIN_CHUSER_EXTEND, new AdminChuserExtendDecoder()),
                        Map.entry(MessageType.JOIN_CHANNEL, new JoinChannelDecoder()),
                        Map.entry(MessageType.QUIT_CHANNEL, new QuitChannelDecoder()),
                        Map.entry(MessageType.CHAT_USER, new ChatUserDecoder()),
                        Map.entry(MessageType.DIRECT_CHAT, new DirectChatDecoder()),
                        Map.entry(MessageType.NOTICE, new NoticeDecoder()),
                        Map.entry(MessageType.KICK, new KickDecoder()),
                        Map.entry(MessageType.SET_USER_FLAG, new SetUserFlagDecoder()),
                        Map.entry(MessageType.SET_SUB_BJ, new SetSubBjDecoder()),
                        Map.entry(MessageType.SET_NICKNAME, new SetNicknameDecoder()),
                        Map.entry(MessageType.SERVER_STAT, new ServerStatDecoder()),
                        Map.entry(MessageType.NULL_16, new Null16Decoder()),
                        Map.entry(MessageType.ICE_MODE, new IceModeDecoder()),
                        Map.entry(MessageType.SEND_FAN_LETTER, new SendFanLetterDecoder()),
                        Map.entry(MessageType.ICE_MODE_EX, new IceModeExDecoder()),
                        Map.entry(MessageType.GET_ICE_MODE_RELAY, new GetIceModeRelayDecoder()),
                        Map.entry(MessageType.RELOAD_BURN_LEVEL, new ReloadBurnLevelDecoder()),
                        Map.entry(MessageType.BLIND_KICK, new BlindKickDecoder()),
                        Map.entry(MessageType.APPEND_DATA, new AppendDataDecoder()),
                        Map.entry(MessageType.BASEBALL_EVENT, new BaseballEventDecoder()),
                        Map.entry(MessageType.PAID_ITEM, new PaidItemDecoder()),
                        Map.entry(MessageType.SNS_MESSAGE, new SnsMessageDecoder()),
                        Map.entry(MessageType.SNS_MODE, new SnsModeDecoder()),
                        Map.entry(MessageType.SEND_BALLOON_SUB, new SendBalloonSubDecoder()),
                        Map.entry(MessageType.SEND_FAN_LETTER_SUB, new SendFanLetterSubDecoder()),
                        Map.entry(MessageType.SEND_ADMIN_NOTICE, new SendAdminNoticeDecoder()),
                        Map.entry(MessageType.BAN_WORD, new BanWordDecoder()),
                        Map.entry(MessageType.FREECAT_OWNER_JOIN, new FreecatOwnerJoinDecoder()),
                        Map.entry(MessageType.KICK_AND_CANCEL, new KickAndCancelDecoder()),
                        Map.entry(MessageType.KICK_USERLIST, new KickUserListDecoder()),
                        Map.entry(MessageType.CLI_DOBAE_INFO, new CliDobaeInfoDecoder()),
                        Map.entry(MessageType.KICK_MSG_STATE, new KickMsgStateDecoder()),
                        Map.entry(MessageType.TRANSLATION, new TranslationDecoder()),
                        Map.entry(MessageType.KEEP_ALIVE, new KeepAliveDecoder()),
                        Map.entry(MessageType.LOGIN, new LoginDecoder()),
                        Map.entry(MessageType.SET_CHANNEL_NAME, new SetChannelNameDecoder()),
                        Map.entry(MessageType.BJ_STICKER_ITEM, new BJStickerItemDecoder()),
                        Map.entry(MessageType.STATION_ADCON, new StationAdconDecoder()),
                        Map.entry(MessageType.NONE_TYPE, new NoneTypeDecoder()),
                        Map.entry(MessageType.TRANSLATION_STATE, new TranslationStateDecoder()),
                        Map.entry(MessageType.SET_DUMB, new SetDumbDecoder())));
    }
}
