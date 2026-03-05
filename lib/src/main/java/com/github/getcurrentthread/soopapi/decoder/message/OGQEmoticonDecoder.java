package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.OGQEmoticonEvent;

public class OGQEmoticonDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 1;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        String chatNo = parts.length >= 6 ? parts[0] : "";
        String message = parts.length >= 6 ? parts[1] : "";
        String groupId = parts.length >= 6 ? parts[2] : "";
        String subId = parts.length >= 6 ? parts[3] : "";
        String version = parts.length >= 6 ? parts[4] : "";
        String userInfo = parts.length >= 6 ? parts[5] : "";
        String color = parts.length >= 9 ? parts[6] : "";
        String chatLang = parts.length >= 9 ? parts[7] : "";
        String type = parts.length >= 9 ? parts[8] : "";

        return new OGQEmoticonEvent(
                chatNo,
                message,
                groupId,
                subId,
                version,
                userInfo,
                color,
                chatLang,
                type,
                ChatEvent.OGQ_EMOTICON,
                raw,
                System.currentTimeMillis());
    }
}
