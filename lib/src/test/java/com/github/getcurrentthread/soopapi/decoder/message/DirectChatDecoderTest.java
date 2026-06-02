package com.github.getcurrentthread.soopapi.decoder.message;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.DirectChatEvent;

class DirectChatDecoderTest {

    private DirectChatDecoder decoder;

    @BeforeEach
    void setup() {
        decoder = new DirectChatDecoder();
    }

    @Test
    void decode_incomingWhisper_mapsSenderAndReceiverConsistently() {
        // 방송인(BJ, peerUser)이 나(myUser)에게 보낸 귓말 — 발신자가 BJ라 type=1 (type은 방향이 아니라 발신자 구분 플래그)
        // 원본 필드 순서: message | receiverId | senderId | type | grade | senderNick | receiverNick |
        // flag | ...
        String[] parts = {
            "받은 귓말", "myUser(2)", "peerUser", "1", "3", "PeerNick", "MyNick", "100", "200", ""
        };

        BaseEvent result = decoder.decode(parts, "raw");

        assertNotNull(result);
        assertInstanceOf(DirectChatEvent.class, result);
        DirectChatEvent event = (DirectChatEvent) result;

        assertEquals("받은 귓말", event.message());
        // 발신자 id와 닉네임이 같은 사람(상대)을 가리켜야 함
        assertEquals("peerUser", event.senderId());
        assertEquals("PeerNick", event.senderNickname());
        // 수신자 id와 닉네임이 같은 사람(나)을 가리켜야 함
        assertEquals("myUser(2)", event.receiverId());
        assertEquals("MyNick", event.receiverNickname());
        assertEquals(1, event.type());
        assertEquals("100", event.flag());
        assertEquals(ChatEvent.DIRECT_CHAT, event.eventType());
    }

    @Test
    void decode_outgoingEcho_mapsSenderAndReceiverConsistently() {
        // 내가(myUser, 일반 사용자) peerUser에게 보낸 귓말의 echo — 발신자가 BJ가 아니라 type=0
        String[] parts = {
            "보낸 귓말", "peerUser", "myUser(2)", "0", "3", "MyNick", "PeerNick", "300", "400", ""
        };

        BaseEvent result = decoder.decode(parts, "raw");

        assertNotNull(result);
        DirectChatEvent event = (DirectChatEvent) result;

        assertEquals("보낸 귓말", event.message());
        // echo에서 발신자는 나
        assertEquals("myUser(2)", event.senderId());
        assertEquals("MyNick", event.senderNickname());
        // 수신자는 상대
        assertEquals("peerUser", event.receiverId());
        assertEquals("PeerNick", event.receiverNickname());
        assertEquals(0, event.type());
    }

    @Test
    void decode_tooFewParts_returnsNull() {
        String[] parts = {"msg", "a", "b", "1", "2", "nick", "0"}; // 7개 (< 8)

        BaseEvent result = decoder.decode(parts, "raw");

        assertNull(result, "Should return null for fewer than 8 parts");
    }

    @Test
    void decode_emptyParts_returnsNull() {
        assertNull(decoder.decode(new String[0], "raw"));
    }
}
