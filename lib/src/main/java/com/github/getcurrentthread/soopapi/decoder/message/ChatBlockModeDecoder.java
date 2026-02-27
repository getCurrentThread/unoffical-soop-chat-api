package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.ChatBlockModeEvent;

public class ChatBlockModeDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new ChatBlockModeEvent(ChatEvent.CHAT_BLOCK_MODE, raw, System.currentTimeMillis());
    }
}
