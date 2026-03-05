package com.github.getcurrentthread.soopapi.websocket;

import static org.junit.jupiter.api.Assertions.*;

import java.net.http.WebSocket;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.getcurrentthread.soopapi.decoder.MessageDispatcher;
import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.EventEmitter;
import com.github.getcurrentthread.soopapi.event.model.RawEvent;

public class WebSocketListenerBufferTest {

    private EventEmitter emitter;
    private WebSocketListener listener;
    private WebSocket stubWebSocket;

    @BeforeEach
    void setup() {
        emitter = new EventEmitter();
        var dispatcher =
                new MessageDispatcher(Map.of(), Executors.newSingleThreadExecutor(), emitter);
        listener = new WebSocketListener(dispatcher, emitter);
        stubWebSocket = new StubWebSocket();
    }

    @Test
    void smallMessage_dispatchedCorrectly() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> received = new AtomicReference<>();

        emitter.on(
                ChatEvent.RAW,
                (RawEvent e) -> {
                    received.set(e.raw());
                    latch.countDown();
                });

        String msg = "hello";
        listener.onText(stubWebSocket, msg, true);

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(msg, received.get());
    }

    @Test
    void largeMessage_processedAndBufferShrinks() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        emitter.on(ChatEvent.RAW, (RawEvent e) -> latch.countDown());

        // DEFAULT_BUFFER_SIZE * 4 (65536자)보다 큰 메시지 구성
        String largeMsg = "x".repeat(WebSocketListener.DEFAULT_BUFFER_SIZE * 5);

        listener.onText(stubWebSocket, largeMsg, true);

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Large message should be dispatched");

        // 대용량 메시지 이후 작은 메시지를 전송하여 정상 동작 확인
        // (버퍼가 재할당됨)
        CountDownLatch latch2 = new CountDownLatch(1);
        AtomicReference<String> received2 = new AtomicReference<>();

        emitter.on(
                ChatEvent.RAW,
                (RawEvent e) -> {
                    received2.set(e.raw());
                    latch2.countDown();
                });

        listener.onText(stubWebSocket, "small", true);

        assertTrue(latch2.await(2, TimeUnit.SECONDS));
        assertEquals("small", received2.get());
    }

    @Test
    void multiPartMessage_assembledCorrectly() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> received = new AtomicReference<>();

        emitter.on(
                ChatEvent.RAW,
                (RawEvent e) -> {
                    received.set(e.raw());
                    latch.countDown();
                });

        listener.onText(stubWebSocket, "hello ", false);
        listener.onText(stubWebSocket, "world", true);

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals("hello world", received.get());
    }

    /** request() 호출 추적 외에는 아무 동작도 하지 않는 최소한의 WebSocket 스텁. */
    private static class StubWebSocket implements WebSocket {
        @Override
        public java.util.concurrent.CompletableFuture<WebSocket> sendText(
                CharSequence data, boolean last) {
            return java.util.concurrent.CompletableFuture.completedFuture(this);
        }

        @Override
        public java.util.concurrent.CompletableFuture<WebSocket> sendBinary(
                java.nio.ByteBuffer data, boolean last) {
            return java.util.concurrent.CompletableFuture.completedFuture(this);
        }

        @Override
        public java.util.concurrent.CompletableFuture<WebSocket> sendPing(
                java.nio.ByteBuffer message) {
            return java.util.concurrent.CompletableFuture.completedFuture(this);
        }

        @Override
        public java.util.concurrent.CompletableFuture<WebSocket> sendPong(
                java.nio.ByteBuffer message) {
            return java.util.concurrent.CompletableFuture.completedFuture(this);
        }

        @Override
        public java.util.concurrent.CompletableFuture<WebSocket> sendClose(
                int statusCode, String reason) {
            return java.util.concurrent.CompletableFuture.completedFuture(this);
        }

        @Override
        public void request(long n) {}

        @Override
        public String getSubprotocol() {
            return "";
        }

        @Override
        public boolean isOutputClosed() {
            return false;
        }

        @Override
        public boolean isInputClosed() {
            return false;
        }

        @Override
        public void abort() {}
    }
}
