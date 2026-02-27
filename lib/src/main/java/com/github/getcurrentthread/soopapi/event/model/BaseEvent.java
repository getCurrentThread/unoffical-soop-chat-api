package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public interface BaseEvent {
    ChatEvent eventType();
    String raw();
    long timestamp();
}
