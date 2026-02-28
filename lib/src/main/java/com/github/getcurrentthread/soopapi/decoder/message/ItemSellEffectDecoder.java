package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.ItemSellEffectEvent;

public class ItemSellEffectDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new ItemSellEffectEvent(ChatEvent.ITEM_SELL_EFFECT, raw, System.currentTimeMillis());
    }
}
