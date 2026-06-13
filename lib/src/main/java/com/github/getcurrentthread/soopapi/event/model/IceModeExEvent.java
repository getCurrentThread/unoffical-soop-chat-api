package com.github.getcurrentthread.soopapi.event.model;

import java.util.Set;

import com.github.getcurrentthread.soopapi.code.ChatIceType;
import com.github.getcurrentthread.soopapi.event.ChatEvent;

public record IceModeExEvent(
        int iceMode,
        int freezeType,
        int balloonLimitCount,
        int subscriptionLimitCount,
        ChatEvent eventType,
        String raw,
        long timestamp)
        implements SystemBaseEvent {

    /** {@code iceMode}를 레거시 {@link ChatIceType}로 지연 변환합니다. */
    public ChatIceType iceType() {
        return ChatIceType.fromCode(iceMode);
    }

    /** {@code iceMode}를 v2 비트 플래그 집합으로 지연 분해합니다. */
    public Set<ChatIceType.Flag> iceFlags() {
        return ChatIceType.Flag.fromMask(iceMode);
    }

    /** {@code iceMode}를 v2 플래그로 해석해야 하는지 여부. */
    public boolean isIceFlagMode() {
        return ChatIceType.isFlagMode(iceMode);
    }
}
