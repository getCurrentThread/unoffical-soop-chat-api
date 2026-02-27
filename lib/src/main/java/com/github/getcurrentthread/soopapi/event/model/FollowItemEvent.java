package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record FollowItemEvent(
        int chatNo,
        String recvId,
        String sendId,
        String sendNick,
        int type,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements BaseEvent {}
