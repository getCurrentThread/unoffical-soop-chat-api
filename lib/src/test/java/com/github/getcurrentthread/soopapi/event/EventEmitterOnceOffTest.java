package com.github.getcurrentthread.soopapi.event;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.getcurrentthread.soopapi.event.model.ChatMessageEvent;

public class EventEmitterOnceOffTest {

    private EventEmitter emitter;

    @BeforeEach
    void setup() {
        emitter = new EventEmitter();
    }

    @Test
    void onceRegisteredListener_canBeRemovedByOff() {
        AtomicInteger callCount = new AtomicInteger(0);
        EventListener<ChatMessageEvent> listener = e -> callCount.incrementAndGet();

        emitter.once(ChatEvent.CHAT_MESSAGE, listener);
        emitter.off(ChatEvent.CHAT_MESSAGE, listener);

        emitter.emit(ChatEvent.CHAT_MESSAGE, createEvent());

        assertEquals(0, callCount.get(), "once() listener should be removable via off()");
    }

    @Test
    void onceRegisteredListener_offDoesNotAffectOtherListeners() {
        AtomicInteger count1 = new AtomicInteger(0);
        AtomicInteger count2 = new AtomicInteger(0);

        EventListener<ChatMessageEvent> listener1 = e -> count1.incrementAndGet();
        EventListener<ChatMessageEvent> listener2 = e -> count2.incrementAndGet();

        emitter.once(ChatEvent.CHAT_MESSAGE, listener1);
        emitter.once(ChatEvent.CHAT_MESSAGE, listener2);

        emitter.off(ChatEvent.CHAT_MESSAGE, listener1);

        emitter.emit(ChatEvent.CHAT_MESSAGE, createEvent());

        assertEquals(0, count1.get(), "Removed listener should not fire");
        assertEquals(1, count2.get(), "Other once() listener should still fire");
    }

    @Test
    void onRegisteredListener_offWorksNormally() {
        AtomicInteger callCount = new AtomicInteger(0);
        EventListener<ChatMessageEvent> listener = e -> callCount.incrementAndGet();

        emitter.on(ChatEvent.CHAT_MESSAGE, listener);
        emitter.off(ChatEvent.CHAT_MESSAGE, listener);

        emitter.emit(ChatEvent.CHAT_MESSAGE, createEvent());

        assertEquals(0, callCount.get(), "on() listener should be removable via off()");
    }

    @Test
    void once_firesExactlyOnce() {
        AtomicInteger callCount = new AtomicInteger(0);
        EventListener<ChatMessageEvent> listener = e -> callCount.incrementAndGet();

        emitter.once(ChatEvent.CHAT_MESSAGE, listener);

        emitter.emit(ChatEvent.CHAT_MESSAGE, createEvent());
        emitter.emit(ChatEvent.CHAT_MESSAGE, createEvent());
        emitter.emit(ChatEvent.CHAT_MESSAGE, createEvent());

        assertEquals(1, callCount.get(), "once() listener should fire exactly once");
    }

    @Test
    void clear_alsoClearsOnceWrapperMappings() {
        AtomicInteger callCount = new AtomicInteger(0);
        EventListener<ChatMessageEvent> listener = e -> callCount.incrementAndGet();

        emitter.once(ChatEvent.CHAT_MESSAGE, listener);
        emitter.clear();

        emitter.emit(ChatEvent.CHAT_MESSAGE, createEvent());

        assertEquals(0, callCount.get(), "clear() should remove all listeners including once()");
    }

    private ChatMessageEvent createEvent() {
        return new ChatMessageEvent(
                "msg",
                "user",
                0,
                0,
                "nick",
                "0",
                "0",
                "",
                "",
                ChatEvent.CHAT_MESSAGE,
                "raw",
                System.currentTimeMillis());
    }
}
