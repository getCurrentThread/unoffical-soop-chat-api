package com.github.getcurrentthread.soopapi.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SOOPChatUtilsTest {

    // --- safeParseInt 테스트 ---

    @Test
    void safeParseInt_validInt() {
        assertEquals(42, SOOPChatUtils.safeParseInt("42", 0));
    }

    @Test
    void safeParseInt_negativeInt() {
        assertEquals(-1, SOOPChatUtils.safeParseInt("-1", 0));
    }

    @Test
    void safeParseInt_nonNumeric_returnsDefault() {
        assertEquals(99, SOOPChatUtils.safeParseInt("abc", 99));
    }

    @Test
    void safeParseInt_emptyString_returnsDefault() {
        assertEquals(99, SOOPChatUtils.safeParseInt("", 99));
    }

    @Test
    void safeParseInt_null_returnsDefault() {
        assertEquals(99, SOOPChatUtils.safeParseInt(null, 99));
    }

    @Test
    void safeParseInt_overflow_returnsDefault() {
        assertEquals(99, SOOPChatUtils.safeParseInt("99999999999", 99));
    }

    // --- safeParseLong 테스트 ---

    @Test
    void safeParseLong_validLong() {
        assertEquals(42L, SOOPChatUtils.safeParseLong("42", 0L));
    }

    @Test
    void safeParseLong_largeLong() {
        assertEquals(9007199254740993L, SOOPChatUtils.safeParseLong("9007199254740993", 0L));
    }

    @Test
    void safeParseLong_nonNumeric_returnsDefault() {
        assertEquals(99L, SOOPChatUtils.safeParseLong("abc", 99L));
    }

    @Test
    void safeParseLong_null_returnsDefault() {
        assertEquals(99L, SOOPChatUtils.safeParseLong(null, 99L));
    }

    // --- utf8ByteLength & calculateByteSize 테스트 ---

    @Test
    void utf8ByteLength_ascii() {
        assertEquals(5, SOOPChatUtils.utf8ByteLength("hello"));
    }

    @Test
    void calculateByteSize_ascii() {
        assertEquals(11, SOOPChatUtils.calculateByteSize("hello"));
    }

    @Test
    void utf8ByteLength_korean() {
        assertEquals(6, SOOPChatUtils.utf8ByteLength("\ud55c\uae00"));
    }

    @Test
    void calculateByteSize_korean() {
        assertEquals(12, SOOPChatUtils.calculateByteSize("\ud55c\uae00"));
    }
}
