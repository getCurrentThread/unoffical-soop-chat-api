package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record StationAdconEvent(
        String bjId,
        String userId,
        String userNickName,
        int adconCount,
        String isDefault,
        String adconMsg,
        String chatNumber,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements DonationBaseEvent {}
