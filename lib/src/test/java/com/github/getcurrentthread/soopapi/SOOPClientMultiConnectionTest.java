package com.github.getcurrentthread.soopapi;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.getcurrentthread.soopapi.client.SOOPChatClient;
import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.StreamEventListener;
import com.github.getcurrentthread.soopapi.event.model.ChatMessageEvent;

public class SOOPClientMultiConnectionTest {

    private SOOPClient client;

    @BeforeEach
    public void setup() {
        client = new SOOPClient();
    }

    @AfterEach
    public void teardown() {
        client.close();
    }

    private static ChatMessageEvent sampleChat(String msg) {
        return new ChatMessageEvent(
                msg,
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

    @Test
    public void add_duplicateBid_returnsSameInstance() {
        SOOPChatClient a = client.add("streamer1");
        SOOPChatClient b = client.add("streamer1");
        assertSame(a, b, "add() with same bid should return same instance");
        assertEquals(1, client.streamerIds().size());
    }

    @Test
    public void add_thenRemove_returnsTrueAndDetaches() {
        client.add("streamer1");
        assertNotNull(client.get("streamer1"));
        assertTrue(client.remove("streamer1"));
        assertNull(client.get("streamer1"));
        assertFalse(client.streamerIds().contains("streamer1"));
    }

    @Test
    public void remove_unknown_returnsFalse() {
        assertFalse(client.remove("nope"));
    }

    @Test
    public void on_beforeAdd_propagatesToFutureStream() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> receivedBid = new AtomicReference<>();
        AtomicReference<ChatMessageEvent> receivedEvent = new AtomicReference<>();

        client.on(
                ChatEvent.CHAT_MESSAGE,
                (String bid, ChatMessageEvent e) -> {
                    receivedBid.set(bid);
                    receivedEvent.set(e);
                    latch.countDown();
                });

        SOOPChatClient a = client.add("streamerA");
        a.getEventEmitter().emit(ChatEvent.CHAT_MESSAGE, sampleChat("hi"));

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals("streamerA", receivedBid.get());
        assertEquals("hi", receivedEvent.get().message());
    }

    @Test
    public void on_afterAdd_propagatesToExistingStreams() throws Exception {
        SOOPChatClient a = client.add("streamerA");
        SOOPChatClient b = client.add("streamerB");

        CountDownLatch latch = new CountDownLatch(2);
        Set<String> seenBids = java.util.Collections.synchronizedSet(new java.util.HashSet<>());

        client.on(
                ChatEvent.CHAT_MESSAGE,
                (String bid, ChatMessageEvent e) -> {
                    seenBids.add(bid);
                    latch.countDown();
                });

        a.getEventEmitter().emit(ChatEvent.CHAT_MESSAGE, sampleChat("a"));
        b.getEventEmitter().emit(ChatEvent.CHAT_MESSAGE, sampleChat("b"));

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(Set.of("streamerA", "streamerB"), seenBids);
    }

    @Test
    public void off_removesFromAllStreams() throws Exception {
        SOOPChatClient a = client.add("streamerA");
        SOOPChatClient b = client.add("streamerB");

        AtomicInteger count = new AtomicInteger();
        StreamEventListener<ChatMessageEvent> listener = (bid, e) -> count.incrementAndGet();

        client.on(ChatEvent.CHAT_MESSAGE, listener);
        client.off(ChatEvent.CHAT_MESSAGE, listener);

        a.getEventEmitter().emit(ChatEvent.CHAT_MESSAGE, sampleChat("a"));
        b.getEventEmitter().emit(ChatEvent.CHAT_MESSAGE, sampleChat("b"));
        Thread.sleep(100);

        assertEquals(0, count.get(), "Listener should not be called after off()");
    }

    @Test
    public void remove_detachesGlobalSub_othersUnaffected() throws Exception {
        SOOPChatClient a = client.add("streamerA");
        SOOPChatClient b = client.add("streamerB");

        AtomicInteger count = new AtomicInteger();
        AtomicReference<String> lastBid = new AtomicReference<>();

        client.on(
                ChatEvent.CHAT_MESSAGE,
                (String bid, ChatMessageEvent e) -> {
                    lastBid.set(bid);
                    count.incrementAndGet();
                });

        client.remove("streamerA");

        // a is closed, emitter cleared — emit via b only
        b.getEventEmitter().emit(ChatEvent.CHAT_MESSAGE, sampleChat("b"));
        Thread.sleep(100);

        assertEquals(1, count.get());
        assertEquals("streamerB", lastBid.get());
    }

    @Test
    public void streamerIds_returnsSnapshot() {
        client.add("a");
        client.add("b");
        client.add("c");
        assertEquals(Set.of("a", "b", "c"), client.streamerIds());

        client.remove("b");
        assertEquals(Set.of("a", "c"), client.streamerIds());
    }
}
