package com.github.getcurrentthread.soopapi;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.getcurrentthread.soopapi.api.SOOPAuth;
import com.github.getcurrentthread.soopapi.api.SOOPChannel;
import com.github.getcurrentthread.soopapi.api.SOOPHttpClient;
import com.github.getcurrentthread.soopapi.api.SOOPLive;
import com.github.getcurrentthread.soopapi.client.SOOPChatClient;
import com.github.getcurrentthread.soopapi.config.SOOPChatConfig;
import com.github.getcurrentthread.soopapi.config.SOOPClientConfig;
import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.EventListener;
import com.github.getcurrentthread.soopapi.event.StreamEventListener;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;

public class SOOPClient implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(SOOPClient.class.getName());

    private final SOOPAuth auth;
    private final SOOPLive live;
    private final SOOPChannel channel;
    private final SOOPHttpClient httpClient;

    private final Map<String, SOOPChatClient> chatClients = new ConcurrentHashMap<>();
    private final Map<ChatEvent, List<GlobalSub<?>>> globalSubs = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public SOOPClient() {
        this(new SOOPClientConfig.Builder().build());
    }

    public SOOPClient(SOOPClientConfig config) {
        this.httpClient = new SOOPHttpClient(config.getConnectionTimeout());
        this.auth = new SOOPAuth(httpClient);
        this.live = new SOOPLive(httpClient);
        this.channel = new SOOPChannel(httpClient);
    }

    public SOOPAuth auth() {
        return auth;
    }

    public SOOPLive live() {
        return live;
    }

    public SOOPChannel channel() {
        return channel;
    }

    /**
     * 기본 설정으로 채팅 클라이언트를 등록합니다.
     *
     * <p>동일 {@code streamerId}로 이미 등록된 클라이언트가 있으면 기존 인스턴스를 반환합니다(중복 등록 방지).
     */
    public SOOPChatClient add(String streamerId) {
        return add(new SOOPChatConfig.Builder().bid(streamerId).build());
    }

    /**
     * 주어진 설정으로 채팅 클라이언트를 등록합니다.
     *
     * <p>{@code config.getBid()} 기준으로 dedup됩니다. 이미 등록된 bid면 <b>기존</b> 인스턴스를 반환하며 전달된 config는 무시됩니다.
     */
    public SOOPChatClient add(SOOPChatConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        String bid = config.getBid();
        if (bid == null || bid.isBlank()) {
            throw new IllegalArgumentException("bid must not be null or blank");
        }
        lock.lock();
        try {
            SOOPChatClient existing = chatClients.get(bid);
            if (existing != null) {
                return existing;
            }
            SOOPChatClient client = new SOOPChatClient(config);
            attachGlobalSubs(bid, client);
            chatClients.put(bid, client);
            return client;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 등록된 채팅 클라이언트를 해제하고 연결을 닫습니다.
     *
     * @return 등록되어 있어 제거가 일어났으면 {@code true}, 없었으면 {@code false}.
     */
    public boolean remove(String streamerId) {
        SOOPChatClient client;
        lock.lock();
        try {
            client = chatClients.remove(streamerId);
            if (client == null) {
                return false;
            }
            detachGlobalSubs(streamerId, client);
        } finally {
            lock.unlock();
        }
        try {
            client.close();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error closing chat client during remove", e);
        }
        return true;
    }

    public SOOPChatClient get(String streamerId) {
        return chatClients.get(streamerId);
    }

    public Set<String> streamerIds() {
        return Set.copyOf(chatClients.keySet());
    }

    public Collection<SOOPChatClient> clients() {
        return Collections.unmodifiableCollection(new LinkedHashMap<>(chatClients).values());
    }

    /** 등록된 모든 클라이언트의 {@code connectToChat()}을 호출하고, <b>모두</b> 종료될 때 완료되는 future를 반환합니다. */
    public CompletableFuture<Void> connectAll() {
        Collection<SOOPChatClient> snapshot;
        lock.lock();
        try {
            snapshot = List.copyOf(chatClients.values());
        } finally {
            lock.unlock();
        }
        CompletableFuture<?>[] futures =
                snapshot.stream()
                        .map(SOOPChatClient::connectToChat)
                        .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
    }

    /** 등록된 <b>모든</b> 스트림과 이후 추가되는 스트림에 streamerId를 함께 전달하는 글로벌 리스너를 등록합니다. */
    public <T extends BaseEvent> SOOPClient on(ChatEvent event, StreamEventListener<T> listener) {
        lock.lock();
        try {
            GlobalSub<T> sub = new GlobalSub<>(event, listener);
            for (Map.Entry<String, SOOPChatClient> entry : chatClients.entrySet()) {
                String bid = entry.getKey();
                SOOPChatClient client = entry.getValue();
                EventListener<T> wrapper = e -> listener.onEvent(bid, e);
                client.on(event, wrapper);
                sub.wrappersByBid.put(bid, wrapper);
            }
            globalSubs.computeIfAbsent(event, _ -> new CopyOnWriteArrayList<>()).add(sub);
        } finally {
            lock.unlock();
        }
        return this;
    }

    /** {@link #on(ChatEvent, StreamEventListener)}으로 등록한 글로벌 리스너를 모든 스트림에서 제거합니다. */
    public <T extends BaseEvent> SOOPClient off(ChatEvent event, StreamEventListener<T> listener) {
        lock.lock();
        try {
            List<GlobalSub<?>> list = globalSubs.get(event);
            if (list == null) {
                return this;
            }
            GlobalSub<?> matched = null;
            for (GlobalSub<?> s : list) {
                if (s.listener == listener) {
                    matched = s;
                    break;
                }
            }
            if (matched == null) {
                return this;
            }
            list.remove(matched);
            detachWrappers(event, matched);
        } finally {
            lock.unlock();
        }
        return this;
    }

    /**
     * 단일 스트리머 편의 메서드. {@link #add(String)}에 위임하며 dedup이 적용됩니다 — 동일 bid로 두 번 호출하면 같은 인스턴스를 반환합니다.
     */
    public SOOPChatClient chat(String streamerId) {
        return add(streamerId);
    }

    /** 단일 스트리머 편의 메서드. {@link #add(SOOPChatConfig)}에 위임하며 dedup이 적용됩니다. */
    public SOOPChatClient chat(SOOPChatConfig config) {
        return add(config);
    }

    @Override
    public void close() {
        lock.lock();
        try {
            for (SOOPChatClient client : chatClients.values()) {
                try {
                    client.close();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error closing chat client", e);
                }
            }
            chatClients.clear();
            globalSubs.clear();
        } finally {
            lock.unlock();
        }
        httpClient.close();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void attachGlobalSubs(String bid, SOOPChatClient client) {
        for (Map.Entry<ChatEvent, List<GlobalSub<?>>> entry : globalSubs.entrySet()) {
            ChatEvent event = entry.getKey();
            for (GlobalSub<?> sub : entry.getValue()) {
                EventListener wrapper = e -> ((StreamEventListener) sub.listener).onEvent(bid, e);
                client.on(event, wrapper);
                sub.wrappersByBid.put(bid, wrapper);
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void detachGlobalSubs(String bid, SOOPChatClient client) {
        for (Map.Entry<ChatEvent, List<GlobalSub<?>>> entry : globalSubs.entrySet()) {
            ChatEvent event = entry.getKey();
            for (GlobalSub<?> sub : entry.getValue()) {
                EventListener<?> wrapper = sub.wrappersByBid.remove(bid);
                if (wrapper != null) {
                    client.off(event, (EventListener) wrapper);
                }
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void detachWrappers(ChatEvent event, GlobalSub<?> sub) {
        for (Map.Entry<String, EventListener<?>> entry : sub.wrappersByBid.entrySet()) {
            SOOPChatClient client = chatClients.get(entry.getKey());
            if (client != null) {
                client.off(event, (EventListener) entry.getValue());
            }
        }
        sub.wrappersByBid.clear();
    }

    private static final class GlobalSub<T extends BaseEvent> {
        final ChatEvent event;
        final StreamEventListener<T> listener;
        final Map<String, EventListener<?>> wrappersByBid = new ConcurrentHashMap<>();

        GlobalSub(ChatEvent event, StreamEventListener<T> listener) {
            this.event = event;
            this.listener = listener;
        }
    }
}
