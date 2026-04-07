package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BanWordEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;

public class BanWordDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        String replaceWord = parts.length > 0 ? parts[0] : "";
        String[] banWordList = parts.length > 1 ? parts[1].split(",") : new String[0];

        return new BanWordEvent(
                replaceWord, banWordList, ChatEvent.BAN_WORD, raw, System.currentTimeMillis());
    }
}
