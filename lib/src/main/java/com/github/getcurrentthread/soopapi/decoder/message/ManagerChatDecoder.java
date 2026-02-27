package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.ManagerChatEvent;

public class ManagerChatDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new ManagerChatEvent(
                parts[0],
                parts[1],
                Integer.parseInt(parts[2]),
                Integer.parseInt(parts[3]),
                parts[4],
                parts[5],
                parts[6],
                ChatEvent.MANAGER_CHAT,
                raw,
                System.currentTimeMillis());
    }
}
