package com.github.getcurrentthread.soopapi.event;

import com.github.getcurrentthread.soopapi.event.model.BaseEvent;

@FunctionalInterface
public interface StreamEventListener<T extends BaseEvent> {
    void onEvent(String streamerId, T event);
}
