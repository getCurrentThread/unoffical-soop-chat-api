package com.github.getcurrentthread.soopapi.event;

import com.github.getcurrentthread.soopapi.event.model.BaseEvent;

@FunctionalInterface
public interface EventListener<T extends BaseEvent> {
    void onEvent(T event);
}
