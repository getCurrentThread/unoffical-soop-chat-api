package com.github.getcurrentthread.soopapi.client;

import com.github.getcurrentthread.soopapi.handler.AbstractMessageHandler;
import com.github.getcurrentthread.soopapi.handler.MessageMapping;
import com.github.getcurrentthread.soopapi.handler.MessageParam;
import com.github.getcurrentthread.soopapi.handler.RawMessage;
import com.github.getcurrentthread.soopapi.model.Message;
import com.github.getcurrentthread.soopapi.model.MessageType;

import java.util.logging.Logger;

public class ExampleMessageHandler extends AbstractMessageHandler {
    private static final Logger LOGGER = Logger.getLogger(ExampleMessageHandler.class.getName());

    @MessageMapping(MessageType.CHAT_MESSAGE)
    private void handleChatMessage(
            @MessageParam("message") String message,
            @MessageParam("senderNickname") String sender,
            @MessageParam("chatLang") int chatLang
    ) {
        LOGGER.info(String.format("[채팅] %s: %s (언어: %d)", sender, message, chatLang));
    }

    @MessageMapping({MessageType.SEND_BALLOON, MessageType.SEND_BALLOON_SUB})
    private void handleBalloon(
            @MessageParam("senderNickname") String sender,
            @MessageParam("count") int count,
            @RawMessage String rawMessage
    ) {
        LOGGER.info(String.format("[풍선] %s님이 %d개의 풍선을 선물했습니다!", sender, count));
        LOGGER.fine("Raw message: " + rawMessage);
    }

    @MessageMapping(MessageType.JOIN_CHANNEL)
    private void handleJoin(
            Message message,
            @MessageParam("chatNo") String chatNo,
            @MessageParam("bjId") String bjId
    ) {
        LOGGER.info(String.format("[입장] 채팅방 %s (BJ: %s)에 입장했습니다.", chatNo, bjId));
        // message 객체를 통해 전체 데이터에 접근 가능
        LOGGER.fine("Full message data: " + message.getData());
    }
}