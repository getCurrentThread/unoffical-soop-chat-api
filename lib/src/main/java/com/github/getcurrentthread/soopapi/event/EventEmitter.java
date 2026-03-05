package com.github.getcurrentthread.soopapi.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.exception.EventEmitterException;

public class EventEmitter {
    private static final Logger LOGGER = Logger.getLogger(EventEmitter.class.getName());

    private final Map<ChatEvent, List<EventListener<? extends BaseEvent>>> listeners =
            new ConcurrentHashMap<>();
    private final Map<ChatEvent, List<EventListener<? extends BaseEvent>>> internalListeners =
            new ConcurrentHashMap<>();
    private final Map<EventListener<?>, EventListener<?>> onceWrappers = new ConcurrentHashMap<>();
    private volatile Consumer<EventEmitterException> errorHandler;

    @SuppressWarnings("unchecked")
    public <T extends BaseEvent> EventEmitter on(ChatEvent event, EventListener<T> listener) {
        listeners
                .computeIfAbsent(event, _ -> new CopyOnWriteArrayList<>())
                .add((EventListener<? extends BaseEvent>) listener);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseEvent> EventEmitter onInternal(
            ChatEvent event, EventListener<T> listener) {
        internalListeners
                .computeIfAbsent(event, _ -> new CopyOnWriteArrayList<>())
                .add((EventListener<? extends BaseEvent>) listener);
        return this;
    }

    /**
     * 이벤트를 한 번만 수신하는 리스너를 등록합니다.
     *
     * <p>주의: 등록된 이벤트가 발생하지 않으면 내부 래퍼가 해제되지 않습니다. 장기 실행 환경에서 반복적으로 once()를 호출하면 메모리 누수가 발생할 수 있으므로,
     * 필요 시 {@link #clear(ChatEvent)} 또는 {@link #off(ChatEvent, EventListener)}로 정리하세요.
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseEvent> EventEmitter once(ChatEvent event, EventListener<T> listener) {
        EventListener<T> wrapper =
                new EventListener<T>() {
                    @Override
                    public void onEvent(T e) {
                        off(event, this);
                        onceWrappers.remove(listener);
                        listener.onEvent(e);
                    }
                };
        onceWrappers.put(listener, wrapper);
        listeners
                .computeIfAbsent(event, _ -> new CopyOnWriteArrayList<>())
                .add((EventListener<? extends BaseEvent>) wrapper);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseEvent> EventEmitter off(ChatEvent event, EventListener<T> listener) {
        List<EventListener<? extends BaseEvent>> list = listeners.get(event);
        if (list != null) {
            if (!list.remove(listener)) {
                EventListener<?> wrapper = onceWrappers.remove(listener);
                if (wrapper != null) {
                    list.remove(wrapper);
                }
            }
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseEvent> void emit(ChatEvent event, T data) {
        emitFromMap(internalListeners, event, data);
        emitFromMap(listeners, event, data);
    }

    @SuppressWarnings("unchecked")
    private <T extends BaseEvent> void emitFromMap(
            Map<ChatEvent, List<EventListener<? extends BaseEvent>>> map, ChatEvent event, T data) {
        List<EventListener<? extends BaseEvent>> list = map.get(event);
        if (list != null) {
            for (EventListener<? extends BaseEvent> listener : list) {
                try {
                    ((EventListener<T>) listener).onEvent(data);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error executing event listener: " + event, e);
                    Consumer<EventEmitterException> handler = errorHandler;
                    if (handler != null) {
                        try {
                            handler.accept(
                                    new EventEmitterException(
                                            "Error executing event listener: " + event, e, event));
                        } catch (Exception handlerEx) {
                            LOGGER.log(Level.SEVERE, "Error executing error handler", handlerEx);
                        }
                    }
                }
            }
        }
    }

    public void setErrorHandler(Consumer<EventEmitterException> errorHandler) {
        this.errorHandler = errorHandler;
    }

    public void clear() {
        listeners.clear();
        onceWrappers.clear();
    }

    public void clearInternal() {
        internalListeners.clear();
    }

    public void clear(ChatEvent event) {
        List<EventListener<? extends BaseEvent>> removed = listeners.remove(event);
        if (removed != null) {
            onceWrappers.entrySet().removeIf(entry -> removed.contains(entry.getValue()));
        }
    }

    public boolean hasListeners(ChatEvent event) {
        List<EventListener<? extends BaseEvent>> list = listeners.get(event);
        if (list != null && !list.isEmpty()) {
            return true;
        }
        List<EventListener<? extends BaseEvent>> internal = internalListeners.get(event);
        return internal != null && !internal.isEmpty();
    }
}
