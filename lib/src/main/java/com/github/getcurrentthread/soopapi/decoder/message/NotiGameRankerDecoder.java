package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.NotiGameRankerEvent;

public class NotiGameRankerDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new NotiGameRankerEvent(ChatEvent.NOTI_GAME_RANKER, raw, System.currentTimeMillis());
    }
}
