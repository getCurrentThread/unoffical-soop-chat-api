package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.BuyGoodsSubEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class BuyGoodsSubDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 8;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new BuyGoodsSubEvent(
                SOOPChatUtils.safeParseInt(parts[1], 0),
                parts[2],
                parts[4],
                parts[5],
                parts[6],
                SOOPChatUtils.safeParseInt(parts[7], 0),
                1,
                ChatEvent.BUY_GOODS_SUB,
                raw,
                System.currentTimeMillis());
    }
}
