package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.DirectChatEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

/**
 * 귓말(다이렉트 채팅) 메시지를 디코딩합니다.
 *
 * <p>프로토콜은 발신/수신 방향과 무관하게 동일한 필드 순서로 보냅니다: {@code message, receiverId, senderId, type, grade,
 * senderNickname, receiverNickname, flag, ...}. 즉 {@code parts[2]}(senderId)가 {@code
 * parts[5]}(senderNickname)와, {@code parts[1]}(receiverId)가 {@code parts[6]}(receiverNickname)와 짝을
 * 이룹니다.
 *
 * <p>{@code type}은 방향 지시자가 <b>아니다</b>. 실측상 발신자가 일반 사용자면 {@code 0}, 방송인(BJ)이면 {@code 1}로 관찰되었다(내가 보낸
 * echo·내가 받은 귓말 모두 포함). 방향은 {@code senderId}/{@code receiverId}를 내 로그인 ID와 비교해 판단하라. id 값에는 런타임
 * {@code (n)} 접미사(예: {@code "myUser(2)"})가 그대로 포함될 수 있다.
 */
public class DirectChatDecoder implements IMessageDecoder {
    private static final int MIN_PARTS = 8;

    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length < MIN_PARTS) {
            return null;
        }
        return new DirectChatEvent(
                parts[0],
                parts[2],
                parts[1],
                SOOPChatUtils.safeParseInt(parts[3], 0),
                parts[5],
                parts[6],
                parts[7],
                ChatEvent.DIRECT_CHAT,
                raw,
                System.currentTimeMillis());
    }
}
