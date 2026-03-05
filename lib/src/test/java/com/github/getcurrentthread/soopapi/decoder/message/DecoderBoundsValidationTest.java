package com.github.getcurrentthread.soopapi.decoder.message;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.github.getcurrentthread.soopapi.event.model.AdInBroadJsonEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.ChatMessageEvent;
import com.github.getcurrentthread.soopapi.event.model.MissionEvent;
import com.github.getcurrentthread.soopapi.event.model.SendBalloonEvent;

class DecoderBoundsValidationTest {

    // --- SendBalloonDecoder (최소 요소 수=10) ---

    @Test
    void sendBalloon_validFullMessage_returnsEvent() {
        SendBalloonDecoder decoder = new SendBalloonDecoder();
        String[] parts = {
            "bjid", "sender", "nick", "5", "1", "", "", "balloon.swf", "1", "0", "tts"
        };

        BaseEvent result = decoder.decode(parts, "raw");

        assertNotNull(result);
        assertInstanceOf(SendBalloonEvent.class, result);
        SendBalloonEvent event = (SendBalloonEvent) result;
        assertEquals("bjid", event.bjId());
        assertEquals("sender", event.senderId());
        assertEquals("nick", event.senderNickname());
        assertEquals(5, event.count());
        assertEquals(1, event.fanOrder());
        assertEquals("balloon.swf", event.fileName());
        assertTrue(event.isDefault());
        assertEquals(0, event.isTopFan());
        assertEquals("tts", event.ttsData());
    }

    @Test
    void sendBalloon_tooFewParts_returnsNull() {
        SendBalloonDecoder decoder = new SendBalloonDecoder();
        String[] parts = {"bjid", "sender", "nick", "5", "1", "", "", "balloon.swf", "1"};

        BaseEvent result = decoder.decode(parts, "raw");

        assertNull(result, "Should return null when parts.length < 10");
    }

    @Test
    void sendBalloon_emptyParts_returnsNull() {
        SendBalloonDecoder decoder = new SendBalloonDecoder();

        BaseEvent result = decoder.decode(new String[0], "raw");

        assertNull(result, "Should return null for empty parts array");
    }

    @Test
    void sendBalloon_invalidIntegerInCount_doesNotThrow() {
        SendBalloonDecoder decoder = new SendBalloonDecoder();
        String[] parts = {
            "bjid", "sender", "nick", "abc", "1", "", "", "balloon.swf", "1", "0", "tts"
        };

        BaseEvent result = assertDoesNotThrow(() -> decoder.decode(parts, "raw"));

        assertNotNull(result);
        SendBalloonEvent event = (SendBalloonEvent) result;
        assertEquals(0, event.count(), "Invalid integer should fall back to default 0");
    }

    // --- MissionDecoder (최소 요소 수=1, GsonUtil.fromJson 사용) ---

    @Test
    void mission_validJson_returnsEvent() {
        MissionDecoder decoder = new MissionDecoder();
        String[] parts = {"{\"type\":\"mission\",\"amount\":100}"};

        BaseEvent result = decoder.decode(parts, "raw");

        assertNotNull(result);
        assertInstanceOf(MissionEvent.class, result);
    }

    @Test
    void mission_emptyParts_returnsNull() {
        MissionDecoder decoder = new MissionDecoder();

        BaseEvent result = decoder.decode(new String[0], "raw");

        assertNull(result, "Should return null for empty parts array");
    }

    @Test
    void mission_invalidJson_returnsNull() {
        MissionDecoder decoder = new MissionDecoder();
        String[] parts = {"not valid json!!"};

        BaseEvent result = decoder.decode(parts, "raw");

        assertNull(result, "Invalid JSON should return null instead of throwing");
    }

    @Test
    void mission_nullJson_returnsNull() {
        MissionDecoder decoder = new MissionDecoder();
        String[] parts = {"null"};

        BaseEvent result = decoder.decode(parts, "raw");

        assertNull(result, "JSON literal 'null' should result in null return");
    }

    // --- AdInBroadJsonDecoder (최소 요소 수=1, GsonUtil.fromJson 사용) ---

    @Test
    void adInBroadJson_validJson_returnsEvent() {
        AdInBroadJsonDecoder decoder = new AdInBroadJsonDecoder();
        String[] parts = {"{\"ad\":\"data\",\"id\":1}"};

        BaseEvent result = decoder.decode(parts, "raw");

        assertNotNull(result);
        assertInstanceOf(AdInBroadJsonEvent.class, result);
    }

    @Test
    void adInBroadJson_emptyParts_returnsNull() {
        AdInBroadJsonDecoder decoder = new AdInBroadJsonDecoder();

        BaseEvent result = decoder.decode(new String[0], "raw");

        assertNull(result, "Should return null for empty parts array");
    }

    @Test
    void adInBroadJson_invalidJson_returnsNull() {
        AdInBroadJsonDecoder decoder = new AdInBroadJsonDecoder();
        String[] parts = {"{{bad json}}"};

        BaseEvent result = decoder.decode(parts, "raw");

        assertNull(result, "Invalid JSON should return null instead of throwing");
    }

    // --- ChatMessageDecoder (최소 요소 수=8, safeParseInt 교차 검증) ---

    @Test
    void chatMessage_invalidIntegersInTypeAndChatLang_doesNotThrow() {
        ChatMessageDecoder decoder = new ChatMessageDecoder();
        String[] parts = {"msg", "user", "skip", "not_int", "also_bad", "nick", "0", "0"};

        BaseEvent result = assertDoesNotThrow(() -> decoder.decode(parts, "raw"));

        assertNotNull(result);
        ChatMessageEvent event = (ChatMessageEvent) result;
        assertEquals(0, event.type(), "Invalid type should fall back to default 0");
        assertEquals(0, event.chatLang(), "Invalid chatLang should fall back to default 0");
    }
}
