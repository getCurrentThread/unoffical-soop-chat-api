package com.github.getcurrentthread.soopapi.decoder;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.getcurrentthread.soopapi.constant.SOOPConstants;
import com.github.getcurrentthread.soopapi.decoder.message.IMessageDecoder;
import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.EventEmitter;
import com.github.getcurrentthread.soopapi.event.model.ChatMessageEvent;
import com.github.getcurrentthread.soopapi.event.model.RawEvent;
import com.github.getcurrentthread.soopapi.event.model.UnknownEvent;

class MessageDispatcherTest {

    private EventEmitter emitter;
    private MessageDispatcher dispatcher;

    @BeforeEach
    void setup() {
        emitter = new EventEmitter();
    }

    @Test
    void nullMessage_isIgnored() {
        dispatcher = createDispatcher(Map.of());
        // 예외가 발생하지 않아야 함
        dispatcher.dispatchMessage(null);
    }

    @Test
    void emptyMessage_isIgnored() {
        dispatcher = createDispatcher(Map.of());
        // 예외가 발생하지 않아야 함
        dispatcher.dispatchMessage("");
    }

    @Test
    void rawEvent_emittedForEveryMessage() throws Exception {
        dispatcher = createDispatcher(Map.of());

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> received = new AtomicReference<>();

        emitter.on(
                ChatEvent.RAW,
                (RawEvent e) -> {
                    received.set(e.raw());
                    latch.countDown();
                });

        String message = "header" + SOOPConstants.F + "body";
        dispatcher.dispatchMessage(message);

        assertTrue(latch.await(2, TimeUnit.SECONDS), "RAW event should be emitted");
        assertEquals(message, received.get());
    }

    @Test
    void messageWithoutSeparator_isIgnored() throws Exception {
        dispatcher = createDispatcher(Map.of());

        CountDownLatch rawLatch = new CountDownLatch(1);
        emitter.on(ChatEvent.RAW, (RawEvent e) -> rawLatch.countDown());

        // F 구분자가 없는 메시지 - RAW 이벤트만 발생해야 함
        dispatcher.dispatchMessage("no_separator_message");

        // RAW 리스너는 여전히 실행되어야 함
        assertTrue(rawLatch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void knownEvent_routedToDecoder() throws Exception {
        IMessageDecoder chatDecoder =
                (parts, raw) ->
                        new ChatMessageEvent(
                                parts[0],
                                parts.length > 1 ? parts[1] : "",
                                0,
                                0,
                                "nick",
                                "0",
                                "0",
                                "",
                                "",
                                ChatEvent.CHAT_MESSAGE,
                                raw,
                                System.currentTimeMillis());

        dispatcher = createDispatcher(Map.of(ChatEvent.CHAT_MESSAGE, chatDecoder));

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<ChatMessageEvent> received = new AtomicReference<>();

        emitter.on(
                ChatEvent.CHAT_MESSAGE,
                (ChatMessageEvent e) -> {
                    received.set(e);
                    latch.countDown();
                });

        // 서비스 코드 5 (CHAT_MESSAGE)로 메시지 구성
        // 헤더 형식: ESC + TAB + "0005" + 길이 + 접미사
        String header = SOOPConstants.ESC + "0005" + "000010" + "00";
        String payload = "Hello" + SOOPConstants.F + "user1";
        String message = header + SOOPConstants.F + payload;

        dispatcher.dispatchMessage(message);

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Chat message event should be dispatched");
        assertEquals("Hello", received.get().message());
    }

    @Test
    void unknownEvent_dispatchedAsUnknown() throws Exception {
        dispatcher = createDispatcher(Map.of());

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<UnknownEvent> received = new AtomicReference<>();

        emitter.on(
                ChatEvent.NONE_TYPE,
                (UnknownEvent e) -> {
                    received.set(e);
                    latch.countDown();
                });

        // 알 수 없는 서비스 코드 (9999) 사용
        String header = SOOPConstants.ESC + "9999" + "000010" + "00";
        String message = header + SOOPConstants.F + "some_data";

        dispatcher.dispatchMessage(message);

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Unknown event should be dispatched");
        assertNotNull(received.get());
    }

    @Test
    void decoderException_doesNotCrashDispatcher() throws Exception {
        IMessageDecoder failingDecoder =
                (parts, raw) -> {
                    throw new RuntimeException("Decoder error");
                };

        dispatcher = createDispatcher(Map.of(ChatEvent.CHAT_MESSAGE, failingDecoder));

        CountDownLatch rawLatch = new CountDownLatch(1);
        emitter.on(ChatEvent.RAW, (RawEvent e) -> rawLatch.countDown());
        emitter.on(ChatEvent.CHAT_MESSAGE, (ChatMessageEvent e) -> {});

        String header = SOOPConstants.ESC + "0005" + "000010" + "00";
        String message = header + SOOPConstants.F + "data";

        // 예외가 발생하지 않아야 함; 오류는 내부적으로 처리됨
        dispatcher.dispatchMessage(message);

        assertTrue(rawLatch.await(2, TimeUnit.SECONDS), "RAW event should still be emitted");
    }

    @Test
    void messageWithNoListeners_isSkipped() {
        dispatcher = createDispatcher(Map.of());

        // 특정 이벤트에 대해 등록된 리스너가 없음
        // 예외가 발생하지 않아야 함
        String header = SOOPConstants.ESC + "0005" + "000010" + "00";
        String message = header + SOOPConstants.F + "data";

        dispatcher.dispatchMessage(message);
        // 여기까지 예외 없이 도달하면 테스트 통과
    }

    private MessageDispatcher createDispatcher(Map<ChatEvent, IMessageDecoder> decoders) {
        return new MessageDispatcher(decoders, Executors.newSingleThreadExecutor(), emitter);
    }
}
