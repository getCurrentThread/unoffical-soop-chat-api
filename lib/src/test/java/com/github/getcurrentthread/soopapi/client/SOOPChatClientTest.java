package com.github.getcurrentthread.soopapi.client;

import com.github.getcurrentthread.soopapi.config.SOOPChatConfig;
import com.github.getcurrentthread.soopapi.model.Message;
import com.github.getcurrentthread.soopapi.model.MessageType;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SOOPChatClientTest {

    @Test
    public void testSOOPChatClientConnection() throws Exception {
        // BID를 실제 방송 BID로 변경하세요
        String testBID = "yjkim5500";

        System.out.println("Starting test with BID: " + testBID);

        SOOPChatConfig config = new SOOPChatConfig.Builder()
                .bid(testBID)
                .build();
        SOOPChatClient client = new SOOPChatClient(config);

        CountDownLatch latch = new CountDownLatch(1000);

        MessageType[] types = new MessageType[]{
//                MessageType.LOGIN,
                MessageType.CHUSER_EXTEND,
//                MessageType.BJ_STICKER_ITEM,
                MessageType.CHAT_USER,
//                MessageType.JOIN_CHANNEL,
                MessageType.CHAT_MESSAGE,
//                MessageType.SET_USER_FLAG,
//                MessageType.NONE_TYPE,
//                MessageType.TRANSLATION_STATE,
//                MessageType.OGQ_EMOTICON,
//                MessageType.BAN_WORD,
//                MessageType.KICK_MSG_STATE,
//                MessageType.SEND_BALLOON,
//                MessageType.FOLLOW_ITEM_EFFECT,
        };
        client.addObserver(new IChatMessageObserver() {
            @Override
            public void notify(Message message) {
                MessageType type = message.getType();
                if ((Arrays.stream(types).anyMatch(messageType -> messageType == type) && message.getData() != null)) {
                    return;
                }
                System.out.println("Received message: " + message);
                latch.countDown();
                System.out.println(latch.getCount());
            }
        });

        System.out.println("Connecting to chat...");
        client.connectToChat().join();
        System.out.println("Connected to chat. Waiting for messages...");

        // 최대 2분 동안 메시지를 기다립니다
        boolean received = latch.await(10000000, TimeUnit.MINUTES);

        if (received) {
            System.out.println("Test passed: Message received within timeout.");
        } else {
            System.out.println("Test failed: No message received within timeout.");
        }

        System.out.println("Disconnecting...");
        client.disconnect();
        System.out.println("Test completed.");
    }
}