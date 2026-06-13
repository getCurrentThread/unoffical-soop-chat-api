package com.github.getcurrentthread.soopapi.code;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.ChatMessageEvent;
import com.github.getcurrentthread.soopapi.event.model.ChatUserEvent;
import com.github.getcurrentthread.soopapi.event.model.IceModeEvent;
import com.github.getcurrentthread.soopapi.event.model.IceModeExEvent;
import com.github.getcurrentthread.soopapi.event.model.LoginEvent;
import com.github.getcurrentthread.soopapi.event.model.QuitChannelEvent;
import com.github.getcurrentthread.soopapi.event.model.SetUserFlagEvent;

/** Verifies the lazy derived accessors wired onto the event records. */
class DerivedAccessorTest {

    @Test
    void quitChannelEvent_quitStatus() {
        QuitChannelEvent event =
                new QuitChannelEvent(6, 0, "nick", "", "", ChatEvent.QUIT_CHANNEL, "raw", 0L);
        assertEquals(ChatQuitStatus.ADMKICK, event.quitStatus());
    }

    @Test
    void iceModeEvent_legacyValue() {
        IceModeEvent event = new IceModeEvent(2, ChatEvent.ICE_MODE, "raw", 0L);
        assertEquals(ChatIceType.FAN, event.iceType());
        assertFalse(event.isIceFlagMode());
        assertTrue(event.iceFlags().isEmpty());
    }

    @Test
    void iceModeEvent_v2FlagValue() {
        IceModeEvent event = new IceModeEvent(32, ChatEvent.ICE_MODE, "raw", 0L);
        assertTrue(event.isIceFlagMode());
        assertEquals(Set.of(ChatIceType.Flag.FAN2), event.iceFlags());
        // a v2 flag value is not a legacy code
        assertEquals(ChatIceType.UNKNOWN, event.iceType());
    }

    @Test
    void iceModeExEvent_v2FlagValue() {
        IceModeExEvent event = new IceModeExEvent(16, 0, 0, 0, ChatEvent.ICE_MODE_EX, "raw", 0L);
        assertTrue(event.isIceFlagMode());
        assertEquals(Set.of(ChatIceType.Flag.NORMAL2), event.iceFlags());
    }

    @Test
    void chatMessageEvent_senderLevel() {
        ChatMessageEvent event =
                new ChatMessageEvent(
                        "msg",
                        "user123",
                        0,
                        0,
                        "TestNick",
                        "81952|32768",
                        "0",
                        "",
                        "",
                        ChatEvent.CHAT_MESSAGE,
                        "raw",
                        0L);
        UserLevel level = event.senderLevel();
        assertTrue(level.has(UserFlag.REALNAME));
        assertTrue(level.has(UserFlag.MOBILE));
        assertTrue(level.has(UserFlag.FANCLUB));
        assertTrue(level.has(UserFlag2.SPECIFY));
    }

    @Test
    void chatUserEntry_level() {
        ChatUserEvent.ChatUserEntry entry =
                new ChatUserEvent.ChatUserEntry("user123", "TestNick", "16|16384");
        UserLevel level = entry.level();
        assertEquals(Set.of(UserFlag.GUEST), level.primary());
        assertEquals(Set.of(UserFlag2.PC), level.secondary());
    }

    @Test
    void loginEvent_userLevel() {
        LoginEvent event = new LoginEvent("user123", "16|0", ChatEvent.LOGIN, "raw", 0L);
        UserLevel level = event.userLevel();
        assertEquals(Set.of(UserFlag.GUEST), level.primary());
        assertTrue(level.secondary().isEmpty());
    }

    @Test
    void setUserFlagEvent_oldAndNewLevel() {
        SetUserFlagEvent event =
                new SetUserFlagEvent(
                        "16|0", "user123", "TestNick", "32|0", ChatEvent.SET_USER_FLAG, "raw", 0L);
        assertEquals(Set.of(UserFlag.GUEST), event.oldLevel().primary());
        assertEquals(Set.of(UserFlag.FANCLUB), event.newLevel().primary());
    }
}
