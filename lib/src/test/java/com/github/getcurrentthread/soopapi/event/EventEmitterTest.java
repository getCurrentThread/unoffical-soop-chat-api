package com.github.getcurrentthread.soopapi.event;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;

import com.github.getcurrentthread.soopapi.event.model.ChatMessageEvent;
import com.github.getcurrentthread.soopapi.event.model.UnknownEvent;

public class EventEmitterTest {

    private EventEmitter emitter;

    @Before
    public void setup() {
        emitter = new EventEmitter();
    }

    @Test
    public void testOnAndEmit() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<ChatMessageEvent> received = new AtomicReference<>();

        emitter.on(
                ChatEvent.CHAT_MESSAGE,
                (ChatMessageEvent e) -> {
                    received.set(e);
                    latch.countDown();
                });

        ChatMessageEvent event =
                new ChatMessageEvent(
                        "hello",
                        "user1",
                        0,
                        0,
                        "nick1",
                        "0",
                        "0",
                        "",
                        "",
                        ChatEvent.CHAT_MESSAGE,
                        "raw",
                        System.currentTimeMillis());

        emitter.emit(ChatEvent.CHAT_MESSAGE, event);

        assertTrue("Event should be received", latch.await(1, TimeUnit.SECONDS));
        assertEquals("hello", received.get().message());
        assertEquals("user1", received.get().senderId());
    }

    @Test
    public void testOnce() throws Exception {
        AtomicInteger callCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);

        emitter.once(
                ChatEvent.CHAT_MESSAGE,
                (ChatMessageEvent e) -> {
                    callCount.incrementAndGet();
                    latch.countDown();
                });

        ChatMessageEvent event =
                new ChatMessageEvent(
                        "msg",
                        "u",
                        0,
                        0,
                        "n",
                        "0",
                        "0",
                        "",
                        "",
                        ChatEvent.CHAT_MESSAGE,
                        "raw",
                        System.currentTimeMillis());

        emitter.emit(ChatEvent.CHAT_MESSAGE, event);
        latch.await(1, TimeUnit.SECONDS);

        emitter.emit(ChatEvent.CHAT_MESSAGE, event);
        Thread.sleep(100);

        assertEquals("Listener should only be called once", 1, callCount.get());
    }

    @Test
    public void testOff() throws Exception {
        AtomicInteger callCount = new AtomicInteger(0);

        EventListener<ChatMessageEvent> listener = e -> callCount.incrementAndGet();

        emitter.on(ChatEvent.CHAT_MESSAGE, listener);
        emitter.off(ChatEvent.CHAT_MESSAGE, listener);

        ChatMessageEvent event =
                new ChatMessageEvent(
                        "msg",
                        "u",
                        0,
                        0,
                        "n",
                        "0",
                        "0",
                        "",
                        "",
                        ChatEvent.CHAT_MESSAGE,
                        "raw",
                        System.currentTimeMillis());

        emitter.emit(ChatEvent.CHAT_MESSAGE, event);
        Thread.sleep(100);

        assertEquals("Listener should not be called after off()", 0, callCount.get());
    }

    @Test
    public void testMultipleListeners() throws Exception {
        CountDownLatch latch = new CountDownLatch(2);

        emitter.on(ChatEvent.CHAT_MESSAGE, (ChatMessageEvent e) -> latch.countDown());
        emitter.on(ChatEvent.CHAT_MESSAGE, (ChatMessageEvent e) -> latch.countDown());

        ChatMessageEvent event =
                new ChatMessageEvent(
                        "msg",
                        "u",
                        0,
                        0,
                        "n",
                        "0",
                        "0",
                        "",
                        "",
                        ChatEvent.CHAT_MESSAGE,
                        "raw",
                        System.currentTimeMillis());

        emitter.emit(ChatEvent.CHAT_MESSAGE, event);

        assertTrue("Both listeners should be called", latch.await(1, TimeUnit.SECONDS));
    }

    @Test
    public void testClear() throws Exception {
        AtomicInteger callCount = new AtomicInteger(0);

        emitter.on(ChatEvent.CHAT_MESSAGE, (ChatMessageEvent e) -> callCount.incrementAndGet());
        emitter.clear();

        ChatMessageEvent event =
                new ChatMessageEvent(
                        "msg",
                        "u",
                        0,
                        0,
                        "n",
                        "0",
                        "0",
                        "",
                        "",
                        ChatEvent.CHAT_MESSAGE,
                        "raw",
                        System.currentTimeMillis());

        emitter.emit(ChatEvent.CHAT_MESSAGE, event);
        Thread.sleep(100);

        assertEquals("No listeners after clear()", 0, callCount.get());
    }

    @Test
    public void testClearSpecificEvent() throws Exception {
        AtomicInteger chatCount = new AtomicInteger(0);
        AtomicInteger unknownCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);

        emitter.on(ChatEvent.CHAT_MESSAGE, (ChatMessageEvent e) -> chatCount.incrementAndGet());
        emitter.on(
                ChatEvent.NONE_TYPE,
                (UnknownEvent e) -> {
                    unknownCount.incrementAndGet();
                    latch.countDown();
                });

        emitter.clear(ChatEvent.CHAT_MESSAGE);

        ChatMessageEvent chatEvent =
                new ChatMessageEvent(
                        "msg",
                        "u",
                        0,
                        0,
                        "n",
                        "0",
                        "0",
                        "",
                        "",
                        ChatEvent.CHAT_MESSAGE,
                        "raw",
                        System.currentTimeMillis());
        UnknownEvent unknownEvent =
                new UnknownEvent(-1, "raw", ChatEvent.NONE_TYPE, "raw", System.currentTimeMillis());

        emitter.emit(ChatEvent.CHAT_MESSAGE, chatEvent);
        emitter.emit(ChatEvent.NONE_TYPE, unknownEvent);

        latch.await(1, TimeUnit.SECONDS);

        assertEquals("Chat listener should be cleared", 0, chatCount.get());
        assertEquals("Unknown listener should still work", 1, unknownCount.get());
    }

    @Test
    public void testMethodChaining() {
        EventEmitter result =
                emitter.on(ChatEvent.CHAT_MESSAGE, (ChatMessageEvent e) -> {})
                        .on(ChatEvent.JOIN_CHANNEL, e -> {});

        assertSame("Method chaining should return same emitter", emitter, result);
    }
}
