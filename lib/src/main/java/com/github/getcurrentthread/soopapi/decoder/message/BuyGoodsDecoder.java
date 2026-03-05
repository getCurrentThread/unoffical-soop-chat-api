package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.BuyGoodsEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class BuyGoodsDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 8;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new BuyGoodsEvent(
                SOOPChatUtils.safeParseInt(parts[1], 0),
                parts[2],
                parts[4],
                parts[5],
                parts[6],
                SOOPChatUtils.safeParseInt(parts[7], 0),
                0,
                ChatEvent.BUY_GOODS,
                raw,
                System.currentTimeMillis());
    }
}
