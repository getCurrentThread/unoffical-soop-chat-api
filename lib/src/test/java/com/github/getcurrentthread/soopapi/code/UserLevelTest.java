package com.github.getcurrentthread.soopapi.code;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

class UserLevelTest {

    @Test
    void parse_bothGroups() {
        UserLevel level = UserLevel.parse("81952|32768");
        assertEquals(Set.of(UserFlag.FANCLUB, UserFlag.MOBILE, UserFlag.REALNAME), level.primary());
        assertEquals(Set.of(UserFlag2.SPECIFY), level.secondary());
    }

    @Test
    void parse_simplePair() {
        UserLevel level = UserLevel.parse("16|16384");
        assertEquals(Set.of(UserFlag.GUEST), level.primary());
        assertEquals(Set.of(UserFlag2.PC), level.secondary());
    }

    @Test
    void has_convenienceChecks() {
        UserLevel level = UserLevel.parse("16|16384");
        assertTrue(level.has(UserFlag.GUEST));
        assertTrue(level.has(UserFlag2.PC));
    }

    @Test
    void parse_nullAndBlankReturnEmpty() {
        assertSame(UserLevel.EMPTY, UserLevel.parse(null));
        assertSame(UserLevel.EMPTY, UserLevel.parse(""));
    }

    @Test
    void parse_nonNumericReturnsEmptySets() {
        UserLevel level = UserLevel.parse("abc|def");
        assertTrue(level.primary().isEmpty());
        assertTrue(level.secondary().isEmpty());
    }

    @Test
    void parse_noPipeTreatsWholeAsPrimary() {
        UserLevel level = UserLevel.parse("16");
        assertEquals(Set.of(UserFlag.GUEST), level.primary());
        assertTrue(level.secondary().isEmpty());
    }

    @Test
    void parse_trailingAndLeadingPipe() {
        UserLevel trailing = UserLevel.parse("16|");
        assertEquals(Set.of(UserFlag.GUEST), trailing.primary());
        assertTrue(trailing.secondary().isEmpty());

        UserLevel leading = UserLevel.parse("|16384");
        assertTrue(leading.primary().isEmpty());
        assertEquals(Set.of(UserFlag2.PC), leading.secondary());
    }
}
