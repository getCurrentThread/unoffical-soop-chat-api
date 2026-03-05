package com.github.getcurrentthread.soopapi.decoder.message;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.ChatMessageEvent;

class ChatMessageDecoderTest {

    private ChatMessageDecoder decoder;

    @BeforeEach
    void setup() {
        decoder = new ChatMessageDecoder();
    }

    @Test
    void decode_fullMessage_returnsCorrectEvent() {
        String[] parts = {
            "Hello World", // 메시지 [0]
            "user123", // 발신자 ID [1]
            "ignored", // [2] (무시됨)
            "0", // 타입 [3]
            "0", // 채팅 언어 [4]
            "TestNick", // 발신자 닉네임 [5]
            "1", // 발신자 플래그 [6]
            "3", // 구독 개월 수 [7]
            "#FF0000", // 랜덤 닉네임 색상 [8]
            "#00FF00" // 랜덤 닉네임 색상 다크모드 [9]
        };

        BaseEvent result = decoder.decode(parts, "raw_message");

        assertNotNull(result);
        assertInstanceOf(ChatMessageEvent.class, result);

        ChatMessageEvent event = (ChatMessageEvent) result;
        assertEquals("Hello World", event.message());
        assertEquals("user123", event.senderId());
        assertEquals(0, event.type());
        assertEquals(0, event.chatLang());
        assertEquals("TestNick", event.senderNickname());
        assertEquals("1", event.senderFlag());
        assertEquals("3", event.subscriptionMonth());
        assertEquals("#FF0000", event.randomNicknameColor());
        assertEquals("#00FF00", event.randomNicknameColorDarkmode());
        assertEquals(ChatEvent.CHAT_MESSAGE, event.eventType());
        assertEquals("raw_message", event.raw());
    }

    @Test
    void decode_minimumParts_returnsEventWithDefaults() {
        // 정확히 8개 요소 (최소 필요 수)
        String[] parts = {"msg", "user", "skip", "1", "2", "nick", "0", "0"};

        BaseEvent result = decoder.decode(parts, "raw");

        assertNotNull(result);
        ChatMessageEvent event = (ChatMessageEvent) result;
        assertEquals("msg", event.message());
        assertEquals("", event.randomNicknameColor());
        assertEquals("", event.randomNicknameColorDarkmode());
    }

    @Test
    void decode_withOnlyNineElements_defaultsDarkmode() {
        String[] parts = {"msg", "user", "skip", "1", "2", "nick", "0", "0", "#color"};

        BaseEvent result = decoder.decode(parts, "raw");

        assertNotNull(result);
        ChatMessageEvent event = (ChatMessageEvent) result;
        assertEquals("#color", event.randomNicknameColor());
        assertEquals("", event.randomNicknameColorDarkmode());
    }

    @Test
    void decode_tooFewParts_returnsNull() {
        // 8개 미만의 요소는 ArrayIndexOutOfBoundsException 대신 null을 반환해야 함
        String[] parts = {"msg", "user", "skip"};

        BaseEvent result = decoder.decode(parts, "raw");

        assertNull(result, "Should return null for insufficient parts");
    }

    @Test
    void decode_emptyParts_returnsNull() {
        BaseEvent result = decoder.decode(new String[0], "raw");

        assertNull(result, "Should return null for empty parts array");
    }

    @Test
    void decode_sevenParts_returnsNull() {
        String[] parts = {"msg", "user", "skip", "1", "2", "nick", "0"};

        BaseEvent result = decoder.decode(parts, "raw");

        assertNull(result, "Should return null for 7 parts (need at least 8)");
    }

    @Test
    void decode_integerTypeField_parsedCorrectly() {
        String[] parts = {"msg", "user", "skip", "42", "7", "nick", "0", "0"};

        BaseEvent result = decoder.decode(parts, "raw");

        assertNotNull(result);
        ChatMessageEvent event = (ChatMessageEvent) result;
        assertEquals(42, event.type());
        assertEquals(7, event.chatLang());
    }

    @Test
    void decode_invalidIntegerField_fallsBackToDefault() {
        String[] parts = {"msg", "user", "skip", "not_a_number", "0", "nick", "0", "0"};

        BaseEvent result = decoder.decode(parts, "raw");

        assertNotNull(result);
        ChatMessageEvent event = (ChatMessageEvent) result;
        assertEquals(0, event.type(), "Invalid integer should fall back to default 0");
    }

    @Test
    void decode_timestampIsPositive() {
        String[] parts = {"msg", "user", "skip", "0", "0", "nick", "0", "0"};

        BaseEvent result = decoder.decode(parts, "raw");

        assertNotNull(result);
        assertTrue(result.timestamp() > 0, "Timestamp should be positive");
    }
}
