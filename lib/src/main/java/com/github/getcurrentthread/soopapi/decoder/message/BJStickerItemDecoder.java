package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.BjStickerItemEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class BJStickerItemDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        int type = parts.length > 0 ? SOOPChatUtils.safeParseInt(parts[0], 0) : 0;
        return new BjStickerItemEvent(
                type, ChatEvent.BJ_STICKER_ITEM, raw, System.currentTimeMillis());
    }
}
