package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.TranslationStateEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class TranslationStateDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        int state = parts.length > 0 ? SOOPChatUtils.safeParseInt(parts[0], 0) : 0;
        return new TranslationStateEvent(
                state, ChatEvent.TRANSLATION_STATE, raw, System.currentTimeMillis());
    }
}
