package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.KickMsgStateEvent;

public class KickMsgStateDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        String chatNo = parts.length >= 2 ? parts[0] : "";
        boolean isHideKickMessage = parts.length >= 2 && "1".equals(parts[1]);
        return new KickMsgStateEvent(
                chatNo,
                isHideKickMessage,
                ChatEvent.KICK_MSG_STATE,
                raw,
                System.currentTimeMillis());
    }
}
