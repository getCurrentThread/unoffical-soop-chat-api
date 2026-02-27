package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record BuyGoodsSubEvent(
    int goodsType, String bjId, String buyerId, String buyerNickname,
    String goodsName, int goodsCount, int relay,
    ChatEvent eventType, String raw, long timestamp
) implements BaseEvent {}
