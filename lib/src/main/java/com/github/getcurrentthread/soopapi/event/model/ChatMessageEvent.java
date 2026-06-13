package com.github.getcurrentthread.soopapi.event.model;

import com.github.getcurrentthread.soopapi.code.UserLevel;
import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record ChatMessageEvent(
        String message,
        String senderId,
        int type,
        int chatLang,
        String senderNickname,
        String senderFlag,
        String subscriptionMonth,
        String randomNicknameColor,
        String randomNicknameColorDarkmode,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements ChatBaseEvent {

    /** {@code senderFlag}("primary|secondary")를 {@link UserLevel}로 지연 파싱합니다. */
    public UserLevel senderLevel() {
        return UserLevel.parse(senderFlag);
    }
}
