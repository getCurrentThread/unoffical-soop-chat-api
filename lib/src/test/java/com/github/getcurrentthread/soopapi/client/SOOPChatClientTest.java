package com.github.getcurrentthread.soopapi.client;

import com.github.getcurrentthread.soopapi.config.SOOPChatConfig;
import com.github.getcurrentthread.soopapi.model.ChannelInfo;
import com.github.getcurrentthread.soopapi.model.Message;
import com.github.getcurrentthread.soopapi.model.MessageType;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class SOOPChatClientTest {
    
    private static final Logger LOGGER = Logger.getLogger(SOOPChatClientTest.class.getName());
    
    @Before
    public void setup() {
        // 로깅 설정
        System.setProperty("java.util.logging.SimpleFormatter.format",
            "[%1$tF %1$tT] [%4$-7s] %2$s: %5$s%6$s%n");
        
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.ALL);
        
        // 기존 핸들러 제거
        Arrays.stream(rootLogger.getHandlers()).forEach(rootLogger::removeHandler);
        
        // 새 콘솔 핸들러 추가
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleFormatter());
        rootLogger.addHandler(handler);
    }

    @Test
    public void testSOOPChatClientConnection() throws Exception {
        String testBID = "toree0409";
        LOGGER.info("Starting test with BID: " + testBID);

        // 먼저 BNO와 채널 정보를 가져옵니다
        String bno = SOOPChatUtils.getBnoFromBid(testBID);
        LOGGER.info("Retrieved BNO: " + bno);
        
        ChannelInfo channelInfo = SOOPChatUtils.getPlayerLive(bno, testBID);
        LOGGER.info("Retrieved channel info: " + channelInfo);

        SOOPChatConfig config = new SOOPChatConfig.Builder()
                .bid(testBID)
                .bno(bno)
                .build();
                
        LOGGER.info("Created config: " + config);
                
        SOOPChatClient client = new SOOPChatClient(config);
        CountDownLatch connectionLatch = new CountDownLatch(1);
        CountDownLatch messageLatch = new CountDownLatch(10000);

        MessageType[] types = new MessageType[]{
            MessageType.CHUSER_EXTEND,
            MessageType.CHAT_USER,
            MessageType.CHAT_MESSAGE,
            MessageType.JOIN_CHANNEL,
            MessageType.QUIT_CHANNEL
        };

        client.addObserver(message -> {
            LOGGER.info("Message received: " + message);
            
            MessageType type = message.getType();
            LOGGER.info("Message type: " + type);
            
            if (Arrays.stream(types).anyMatch(t -> t == type)) {
                LOGGER.info("Matched message type: " + type);
                messageLatch.countDown();
            }
        });

        LOGGER.info("Connecting to chat...");
        
        try {
            client.connectToChat()
                .thenRun(() -> {
                    LOGGER.info("Connection future completed");
                    connectionLatch.countDown();
                })
                .exceptionally(throwable -> {
                    LOGGER.log(Level.SEVERE, "Connection error", throwable);
                    return null;
                });

            // 연결 대기
            boolean connected = connectionLatch.await(30, TimeUnit.SECONDS);
            if (!connected) {
                LOGGER.warning("Failed to establish connection within timeout");
                return;
            }

            LOGGER.info("Connected successfully, waiting for messages...");
            
            // 메시지 대기
            boolean received = messageLatch.await(1800, TimeUnit.SECONDS);

            if (received) {
                LOGGER.info("Test passed: Message received");
            } else {
                LOGGER.warning("Test failed: Timeout waiting for messages");
                LOGGER.info("Client connected: " + client.isConnected());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Test encountered an error", e);
            throw e;
        } finally {
            LOGGER.info("Disconnecting...");
            client.disconnect();
            LOGGER.info("Test completed.");
        }
    }
}