package com.github.getcurrentthread.soopapi.code;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

class UserFlag2Test {

    @Test
    void fromMask_decomposesSample163840() {
        // 163840 = 131072 | 32768 = HTML5 | SPECIFY
        assertEquals(Set.of(UserFlag2.HTML5, UserFlag2.SPECIFY), UserFlag2.fromMask(163840));
    }

    @Test
    void fromMask_singleBit() {
        assertEquals(Set.of(UserFlag2.SPECIFY), UserFlag2.fromMask(32768));
        assertEquals(Set.of(UserFlag2.PC), UserFlag2.fromMask(16384));
        assertEquals(Set.of(UserFlag2.GLOBAL_PC), UserFlag2.fromMask(1));
    }

    @Test
    void fromMask_followerPeriod36ValueResolvesToBit22() {
        // doc annotates "1 << 24" but the actual value 4194304 is 1 << 22
        assertEquals(4194304, UserFlag2.FOLLOWER_PERIOD_36.getBit());
        assertEquals(Set.of(UserFlag2.FOLLOWER_PERIOD_36), UserFlag2.fromMask(4194304));
    }

    @Test
    void numericValuesCollideWithPrimaryButDecodeIndependently() {
        // 16 = GUEST in the primary group, GAMEGOD in the secondary group
        assertEquals(Set.of(UserFlag.GUEST), UserFlag.fromMask(16));
        assertEquals(Set.of(UserFlag2.GAMEGOD), UserFlag2.fromMask(16));
    }

    @Test
    void fromMask_unknownBitIgnored() {
        // bit 26 is not a defined secondary flag
        assertTrue(UserFlag2.fromMask(1 << 26).isEmpty());
    }

    @Test
    void toMask_isInverseOfFromMask() {
        for (int mask : new int[] {163840, 32768, 16384, 1, 16, 4194304}) {
            assertEquals(mask, UserFlag2.toMask(UserFlag2.fromMask(mask)));
        }
    }
}
