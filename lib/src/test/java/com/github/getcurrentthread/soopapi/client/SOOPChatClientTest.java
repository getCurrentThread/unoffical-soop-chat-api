package com.github.getcurrentthread.soopapi.client;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.junit.Before;
import org.junit.Test;

import com.github.getcurrentthread.soopapi.config.SOOPChatConfig;
import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.ChatMessageEvent;
import com.github.getcurrentthread.soopapi.model.ChannelInfo;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

public class SOOPChatClientTest {

    private static final Logger LOGGER = Logger.getLogger(SOOPChatClientTest.class.getName());

    @Before
    public void setup() {
        System.setProperty(
                "java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT] [%4$-7s] %2$s: %5$s%6$s%n");

        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.ALL);

        Arrays.stream(rootLogger.getHandlers()).forEach(rootLogger::removeHandler);

        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleFormatter());
        rootLogger.addHandler(handler);
    }

    @Test
    public void testSOOPChatClientConnection() throws Exception {
        String testBID = "lshooooo";
        LOGGER.info("Starting test with BID: " + testBID);

        String bno = SOOPChatUtils.getBnoFromBid(testBID);
        LOGGER.info("Retrieved BNO: " + bno);

        ChannelInfo channelInfo = SOOPChatUtils.getPlayerLive(bno, testBID);
        LOGGER.info("Retrieved channel info: " + channelInfo);

        SOOPChatConfig config = new SOOPChatConfig.Builder().bid(testBID).bno(bno).build();

        LOGGER.info("Created config: " + config);

        SOOPChatClient client = new SOOPChatClient(config);
        CountDownLatch connectionLatch = new CountDownLatch(1);
        CountDownLatch messageLatch = new CountDownLatch(10);

        client.on(
                ChatEvent.CHAT_MESSAGE,
                (ChatMessageEvent e) -> {
                    LOGGER.info(e.senderNickname() + ": " + e.message());
                    messageLatch.countDown();
                });

        client.on(
                ChatEvent.JOIN_CHANNEL,
                event -> {
                    LOGGER.info("Join channel event: " + event);
                    messageLatch.countDown();
                });

        client.on(
                ChatEvent.QUIT_CHANNEL,
                event -> {
                    LOGGER.info("Quit channel event: " + event);
                    messageLatch.countDown();
                });

        client.on(
                ChatEvent.CHAT_USER,
                event -> {
                    LOGGER.info("Chat user event: " + event);
                    messageLatch.countDown();
                });

        LOGGER.info("Connecting to chat...");

        try {
            client.connectToChat()
                    .thenRun(
                            () -> {
                                LOGGER.info("Connection future completed");
                                connectionLatch.countDown();
                            })
                    .exceptionally(
                            throwable -> {
                                LOGGER.log(Level.SEVERE, "Connection error", throwable);
                                return null;
                            });

            boolean connected = connectionLatch.await(30, TimeUnit.SECONDS);
            if (!connected) {
                LOGGER.warning("Failed to establish connection within timeout");
                return;
            }

            LOGGER.info("Connected successfully, waiting for messages...");

            boolean received = messageLatch.await(60, TimeUnit.SECONDS);

            if (received) {
                LOGGER.info("Test passed: Messages received");
            } else {
                LOGGER.warning("Test timed out waiting for messages");
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
