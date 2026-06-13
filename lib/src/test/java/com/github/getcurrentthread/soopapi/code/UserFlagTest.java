package com.github.getcurrentthread.soopapi.code;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

class UserFlagTest {

    @Test
    void fromMask_decomposesSample81952() {
        // 81952 = 65536 | 16384 | 32 = REALNAME | MOBILE | FANCLUB
        assertEquals(
                Set.of(UserFlag.FANCLUB, UserFlag.MOBILE, UserFlag.REALNAME),
                UserFlag.fromMask(81952));
    }

    @Test
    void fromMask_decomposesSample720928() {
        // 720928 = 524288 | 131072 | 65536 | 32 = QUICKVIEW | NODIRECT | REALNAME | FANCLUB
        assertEquals(
                Set.of(UserFlag.FANCLUB, UserFlag.REALNAME, UserFlag.NODIRECT, UserFlag.QUICKVIEW),
                UserFlag.fromMask(720928));
    }

    @Test
    void fromMask_singleBit() {
        assertEquals(Set.of(UserFlag.GUEST), UserFlag.fromMask(16));
        assertEquals(Set.of(UserFlag.ADMIN), UserFlag.fromMask(1));
    }

    @Test
    void fromMask_signBitFlag() {
        // NOTITOPFAN = 1 << 31 is a negative signed int; masking must still work
        assertEquals(Set.of(UserFlag.NOTITOPFAN), UserFlag.fromMask(1 << 31));
        assertEquals(
                Set.of(UserFlag.NOTIVODBALLOON, UserFlag.NOTITOPFAN),
                UserFlag.fromMask(0xC0000000));
    }

    @Test
    void fromMask_unknownBitIgnored() {
        // bit 22 (4194304) is not a defined primary flag
        assertTrue(UserFlag.fromMask(1 << 22).isEmpty());
    }

    @Test
    void toMask_isInverseOfFromMask() {
        for (int mask : new int[] {81952, 720928, 16, 1, 1 << 31, 0xC0000000}) {
            assertEquals(mask, UserFlag.toMask(UserFlag.fromMask(mask)));
        }
    }
}
