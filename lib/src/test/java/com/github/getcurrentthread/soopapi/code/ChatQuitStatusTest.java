package com.github.getcurrentthread.soopapi.code;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ChatQuitStatusTest {

    @Test
    void fromCode_mapsKnownCodes() {
        assertEquals(ChatQuitStatus.CLOSED, ChatQuitStatus.fromCode(0));
        assertEquals(ChatQuitStatus.NORMAL, ChatQuitStatus.fromCode(1));
        assertEquals(ChatQuitStatus.KCIK, ChatQuitStatus.fromCode(2));
        assertEquals(ChatQuitStatus.DUMB, ChatQuitStatus.fromCode(3));
        assertEquals(ChatQuitStatus.OVERCHAT, ChatQuitStatus.fromCode(4));
        assertEquals(ChatQuitStatus.BLIND, ChatQuitStatus.fromCode(5));
        assertEquals(ChatQuitStatus.ADMKICK, ChatQuitStatus.fromCode(6));
    }

    @Test
    void getCode_roundTrips() {
        for (ChatQuitStatus s : ChatQuitStatus.values()) {
            assertEquals(s, ChatQuitStatus.fromCode(s.getCode()));
        }
    }

    @Test
    void fromCode_unknownReturnsSentinel() {
        assertEquals(ChatQuitStatus.UNKNOWN, ChatQuitStatus.fromCode(99));
        assertEquals(ChatQuitStatus.UNKNOWN, ChatQuitStatus.fromCode(7));
    }
}
