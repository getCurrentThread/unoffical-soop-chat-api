package com.github.getcurrentthread.soopapi.code;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

class ChatIceTypeTest {

    @Test
    void fromCode_mapsLegacyGroup() {
        assertEquals(ChatIceType.CLEAR, ChatIceType.fromCode(0));
        assertEquals(ChatIceType.NORMAL, ChatIceType.fromCode(1));
        assertEquals(ChatIceType.FAN, ChatIceType.fromCode(2));
        assertEquals(ChatIceType.SUP, ChatIceType.fromCode(3));
        assertEquals(ChatIceType.FAN_SUP, ChatIceType.fromCode(4));
    }

    @Test
    void fromCode_unknownReturnsSentinel() {
        assertEquals(ChatIceType.UNKNOWN, ChatIceType.fromCode(7));
        // v2 flag values are not legacy codes
        assertEquals(ChatIceType.UNKNOWN, ChatIceType.fromCode(16));
    }

    @Test
    void isFlagMode_discriminatesGroups() {
        assertFalse(ChatIceType.isFlagMode(0));
        assertFalse(ChatIceType.isFlagMode(4));
        assertTrue(ChatIceType.isFlagMode(16));
        assertTrue(ChatIceType.isFlagMode(256));
        assertTrue(ChatIceType.isFlagMode(32 | 256));
    }

    @Test
    void flagFromMask_decomposesBits() {
        assertEquals(Set.of(ChatIceType.Flag.NORMAL2), ChatIceType.Flag.fromMask(16));
        assertEquals(Set.of(ChatIceType.Flag.FAN2), ChatIceType.Flag.fromMask(32));
        assertEquals(Set.of(ChatIceType.Flag.TOP_FAN2), ChatIceType.Flag.fromMask(128));
        assertEquals(
                Set.of(ChatIceType.Flag.FAN2, ChatIceType.Flag.FOLLOWER2),
                ChatIceType.Flag.fromMask(32 | 256));
    }

    @Test
    void flagFromMask_zeroIsEmpty() {
        // duplicate-0 edge: CLEAR2(0) is excluded; cleared == empty set
        assertTrue(ChatIceType.Flag.fromMask(0).isEmpty());
    }

    @Test
    void duplicateZero_resolvesDeterministically() {
        assertEquals(ChatIceType.CLEAR, ChatIceType.fromCode(0));
        assertTrue(ChatIceType.Flag.fromMask(0).isEmpty());
    }
}
