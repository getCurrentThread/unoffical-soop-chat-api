package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record BjStickerItemEvent(int type, ChatEvent eventType, String raw, long timestamp)
        implements ItemBaseEvent {}
