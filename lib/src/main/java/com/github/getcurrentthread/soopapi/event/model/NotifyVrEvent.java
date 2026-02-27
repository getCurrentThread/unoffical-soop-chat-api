package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record NotifyVrEvent(
        int action,
        String bjId,
        String vrId,
        String rtmp,
        String hls,
        int vrType,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements BaseEvent {}
