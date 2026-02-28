package com.github.getcurrentthread.soopapi.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.getcurrentthread.soopapi.event.model.BaseEvent;

public class EventEmitter {
    private static final Logger LOGGER = Logger.getLogger(EventEmitter.class.getName());

    private final Map<ChatEvent, List<EventListener<? extends BaseEvent>>> listeners =
            new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends BaseEvent> EventEmitter on(ChatEvent event, EventListener<T> listener) {
        listeners
                .computeIfAbsent(event, _ -> new CopyOnWriteArrayList<>())
                .add((EventListener<? extends BaseEvent>) listener);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseEvent> EventEmitter once(ChatEvent event, EventListener<T> listener) {
        EventListener<T> wrapper =
                new EventListener<T>() {
                    @Override
                    public void onEvent(T e) {
                        off(event, this);
                        listener.onEvent(e);
                    }
                };
        listeners
                .computeIfAbsent(event, _ -> new CopyOnWriteArrayList<>())
                .add((EventListener<? extends BaseEvent>) wrapper);
        return this;
    }

    public <T extends BaseEvent> EventEmitter off(ChatEvent event, EventListener<T> listener) {
        List<EventListener<? extends BaseEvent>> list = listeners.get(event);
        if (list != null) {
            list.remove(listener);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseEvent> void emit(ChatEvent event, T data) {
        List<EventListener<? extends BaseEvent>> list = listeners.get(event);
        if (list != null) {
            for (EventListener<? extends BaseEvent> listener : list) {
                try {
                    ((EventListener<T>) listener).onEvent(data);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "이벤트 리스너 실행 중 오류 발생: " + event, e);
                }
            }
        }
    }

    public void clear() {
        listeners.clear();
    }

    public void clear(ChatEvent event) {
        listeners.remove(event);
    }

    public boolean hasListeners(ChatEvent event) {
        List<EventListener<? extends BaseEvent>> list = listeners.get(event);
        return list != null && !list.isEmpty();
    }
}
