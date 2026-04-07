package com.github.getcurrentthread.soopapi.client;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.github.getcurrentthread.soopapi.api.SOOPHttpClient;
import com.github.getcurrentthread.soopapi.api.SOOPLive;
import com.github.getcurrentthread.soopapi.api.model.AuthCookie;
import com.github.getcurrentthread.soopapi.api.model.LiveDetail;
import com.github.getcurrentthread.soopapi.config.SOOPChatConfig;
import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.ChatMessageEvent;
import com.github.getcurrentthread.soopapi.exception.AuthenticationException;
import com.github.getcurrentthread.soopapi.model.ChannelInfo;

public class SOOPChatClientTest {

    private static final Logger LOGGER = Logger.getLogger(SOOPChatClientTest.class.getName());

    @BeforeEach
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
    @Tag("integration")
    public void testSOOPChatClientConnection() throws Exception {
        String testBID = "lshooooo";
        LOGGER.info("Starting test with BID: " + testBID);

        SOOPLive soopLive = new SOOPLive(new SOOPHttpClient());
        String bno = soopLive.getBno(testBID).join();
        LOGGER.info("Retrieved BNO: " + bno);

        LiveDetail liveDetail = soopLive.detail(testBID, bno).join();
        ChannelInfo channelInfo = soopLive.toChannelInfo(liveDetail);
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

    @Test
    void sendChat_withoutAuth_throwsAuthenticationException() {
        SOOPChatConfig config =
                new SOOPChatConfig.Builder().bid("testStreamer").bno("12345").build();

        SOOPChatClient client = new SOOPChatClient(config);

        ExecutionException ex =
                assertThrows(ExecutionException.class, () -> client.sendChat("Hello!").get());

        assertInstanceOf(AuthenticationException.class, ex.getCause());
    }

    @Test
    void sendChat_withFailedAuthCookie_throwsAuthenticationException() {
        AuthCookie failedCookie =
                new AuthCookie(
                        "user", false, "", null, null, null, null, null, null, null, null, null,
                        null);

        SOOPChatConfig config =
                new SOOPChatConfig.Builder()
                        .bid("testStreamer")
                        .bno("12345")
                        .authCookie(failedCookie)
                        .build();

        SOOPChatClient client = new SOOPChatClient(config);

        ExecutionException ex =
                assertThrows(ExecutionException.class, () -> client.sendChat("Hello!").get());

        assertInstanceOf(AuthenticationException.class, ex.getCause());
    }

    @Test
    void sendChat_withoutAuth_prioritizesAuthOverConnection() {
        SOOPChatConfig config =
                new SOOPChatConfig.Builder().bid("testStreamer").bno("12345").build();

        SOOPChatClient client = new SOOPChatClient(config);

        // 미연결 + 미인증 상태에서 인증 오류가 먼저 발생해야 함
        assertFalse(client.isConnected());

        ExecutionException ex =
                assertThrows(ExecutionException.class, () -> client.sendChat("Hello!").get());

        assertInstanceOf(
                AuthenticationException.class,
                ex.getCause(),
                "Authentication error should occur before connection error");
    }

    @Test
    void constructor_withoutBno_doesNotThrow() {
        SOOPChatConfig config = new SOOPChatConfig.Builder().bid("testStreamer").build();

        assertDoesNotThrow(() -> new SOOPChatClient(config));
    }

    @Test
    void constructor_withNullConfig_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new SOOPChatClient(null));
    }

    @Test
    void constructor_withNullBid_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new SOOPChatConfig.Builder().build());
    }
}
