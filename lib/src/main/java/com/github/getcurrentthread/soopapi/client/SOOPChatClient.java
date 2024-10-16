package com.github.getcurrentthread.soopapi.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;

import com.github.getcurrentthread.soopapi.decoder.AdconEffectDecoder;
import com.github.getcurrentthread.soopapi.decoder.ChatIMessageDecoder;
import com.github.getcurrentthread.soopapi.decoder.ChocolateDecoder;
import com.github.getcurrentthread.soopapi.decoder.GemItemDecoder;
import com.github.getcurrentthread.soopapi.decoder.GiftOGQEmoticonDecoder;
import com.github.getcurrentthread.soopapi.decoder.GiftSubscriptionDecoder;
import com.github.getcurrentthread.soopapi.decoder.GiftTicketDecoder;
import com.github.getcurrentthread.soopapi.decoder.IMessageDecoder;
import com.github.getcurrentthread.soopapi.decoder.ItemDropsDecoder;
import com.github.getcurrentthread.soopapi.decoder.LiveCaptionDecoder;
import com.github.getcurrentthread.soopapi.decoder.ManagerChatDecoder;
import com.github.getcurrentthread.soopapi.decoder.OGQEmoticonDecoder;
import com.github.getcurrentthread.soopapi.decoder.QuickViewDecoder;
import com.github.getcurrentthread.soopapi.decoder.SendBalloonDecoder;
import com.github.getcurrentthread.soopapi.decoder.SetAdminFlagDecoder;
import com.github.getcurrentthread.soopapi.decoder.SlowModeDecoder;
import com.github.getcurrentthread.soopapi.decoder.VODBalloonDecoder;
import com.github.getcurrentthread.soopapi.decoder.VideoBalloonDecoder;
import com.github.getcurrentthread.soopapi.model.ChannelInfo;
import com.github.getcurrentthread.soopapi.model.MessageType;
import com.github.getcurrentthread.soopapi.util.DefaultSSLContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SOOPChatClient implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(SOOPChatClient.class.getName());
    private static final String F = "\u000c";
    private static final String ESC = "\u001b\t";

    private final ScheduledExecutorService scheduler;
    private final List<IChatMessageObserver> observers = new CopyOnWriteArrayList<>();
    private final Map<MessageType, IMessageDecoder> messageDecoders;

    private WebSocket webSocket;
    private volatile boolean isConnected = false;

    private final String bid;
    private final String bno;
    private final SSLContext sslContext;

    public String getBid() {
        return this.bid;
    }

    public boolean isConnected() {
        return this.isConnected;
    }

    private SOOPChatClient(Builder builder) throws Exception {
        this.bid = Objects.requireNonNull(builder.bid, "BID must not be null");
        this.bno = builder.bno != null ? builder.bno : getBnoFromBid(this.bid);
        this.sslContext = builder.sslContext != null ? builder.sslContext : getDefaultSSLContext();
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.messageDecoders = initializeMessageDecoders();
    }

    private SSLContext getDefaultSSLContext() {
        try {
            return DefaultSSLContext.getInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize default SSL context", e);
        }
    }

    private Map<MessageType, IMessageDecoder> initializeMessageDecoders() {
        return Map.ofEntries(
            Map.entry(MessageType.CHAT_MESSAGE, new ChatIMessageDecoder()),
            Map.entry(MessageType.SEND_BALLOON, new SendBalloonDecoder()),
            Map.entry(MessageType.OGQ_EMOTICON, new OGQEmoticonDecoder()),
            Map.entry(MessageType.OGQ_EMOTICON_GIFT, new GiftOGQEmoticonDecoder()),
            Map.entry(MessageType.MANAGER_CHAT, new ManagerChatDecoder()),
            Map.entry(MessageType.CHOCOLATE, new ChocolateDecoder()),
            Map.entry(MessageType.CHOCOLATE_SUB, new ChocolateDecoder()),
            Map.entry(MessageType.SEND_QUICK_VIEW, new QuickViewDecoder()),
            Map.entry(MessageType.GIFT_TICKET, new GiftTicketDecoder()),
            Map.entry(MessageType.VOD_BALLOON, new VODBalloonDecoder()),
            Map.entry(MessageType.ADCON_EFFECT, new AdconEffectDecoder()),
            Map.entry(MessageType.VIDEO_BALLOON, new VideoBalloonDecoder()),
            Map.entry(MessageType.SEND_SUBSCRIPTION, new GiftSubscriptionDecoder()),
            Map.entry(MessageType.ITEM_DROPS, new ItemDropsDecoder()),
            Map.entry(MessageType.GEM_ITEM_SEND, new GemItemDecoder()),
            Map.entry(MessageType.LIVE_CAPTION, new LiveCaptionDecoder()),
            Map.entry(MessageType.SLOW_MODE, new SlowModeDecoder()),
            Map.entry(MessageType.SET_ADMIN_FLAG, new SetAdminFlagDecoder())
        );
    }

    public void addObserver(IChatMessageObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(IChatMessageObserver observer) {
        observers.remove(observer);
    }

    public CompletableFuture<Void> connectToChat() {
        return CompletableFuture.runAsync(() -> {
            try {
                ChannelInfo channelInfo = getPlayerLive(bno, bid);
                if (channelInfo == null) {
                    throw new IllegalStateException("Failed to retrieve channel information.");
                }

                String wsUrl = String.format("wss://%s:%s/Websocket/%s", channelInfo.CHDOMAIN, channelInfo.CHPT, bid);
                webSocket = HttpClient.newBuilder()
                        .sslContext(sslContext)
                        .build()
                        .newWebSocketBuilder()
                        .subprotocols("chat")
                        .buildAsync(URI.create(wsUrl), createWebSocketListener())
                        .join();

                sendInitialPackets(channelInfo);
                startPingScheduler();

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error occurred while connecting to chat", e);
                throw new CompletionException(e);
            }
        });
    }

    private WebSocket.Listener createWebSocketListener() {
        return new WebSocket.Listener() {
            @Override
            public void onOpen(WebSocket webSocket) {
                isConnected = true;
                LOGGER.info("WebSocket connection opened");
                webSocket.request(1);
            }

            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                handleMessage(data.toString());
                webSocket.request(1);
                return null;
            }

            @Override
            public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
                handleMessage(new String(data.array(), java.nio.charset.StandardCharsets.UTF_8));
                webSocket.request(1);
                return null;
            }

            @Override
            public void onError(WebSocket webSocket, Throwable error) {
                LOGGER.log(Level.SEVERE, "WebSocket error occurred", error);
            }

            @Override
            public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                isConnected = false;
                LOGGER.log(Level.INFO, "WebSocket connection closed: {0}", reason);
                return null;
            }
        };
    }

    private void handleMessage(String message) {
        try {
            Map<String, Object> decodedMessage = decodeMessage(message);
            if (decodedMessage != null) {
                notifyObservers(decodedMessage);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error handling message", e);
        }
    }

    public Map<String, Object> decodeMessage(String message) {
        String[] parts = message.split(F);
        if (parts.length < 2) {
            return null;
        }

        int serviceCode = parseServiceCode(parts[0]);
        MessageType messageType = MessageType.fromCode(serviceCode);

        IMessageDecoder decoder = messageDecoders.getOrDefault(messageType, _parts -> null);
        Map<String, Object> decodedMessage = decoder.decode(parts);

        if (decodedMessage != null) {
            decodedMessage.put("type", messageType.name());
        }

        return decodedMessage;
    }

    private void notifyObservers(Map<String, Object> message) {
        for (IChatMessageObserver observer : observers) {
            try {
                observer.notify(message);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error notifying observer", e);
            }
        }
    }

    private void sendInitialPackets(ChannelInfo channelInfo) {
        String connectPacket = ESC + "000100000600" + F.repeat(3) + "16" + F;
        String joinPacket = ESC + "0002" + String.format("%06d", calculateByteSize(channelInfo.CHATNO)) + "00" + F + channelInfo.CHATNO + F.repeat(5);

        webSocket.sendText(connectPacket, true)
                .thenRun(() -> {
                    try {
                        Thread.sleep(3000);
                        webSocket.sendText(joinPacket, true);
                    } catch (InterruptedException e) {
                        LOGGER.log(Level.WARNING, "Interrupted while sending initial packets", e);
                        Thread.currentThread().interrupt();
                    }
                });
    }

    private void startPingScheduler() {
        String pingPacket = ESC + "000000000100" + F;
        scheduler.scheduleAtFixedRate(() -> {
            if (isConnected) {
                webSocket.sendText(pingPacket, true);
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

    public static String getBnoFromBid(String bid) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://play.sooplive.co.kr/" + bid))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();

        Pattern pattern = Pattern.compile("<meta property=\"og:image\" content=\"https://liveimg\\.sooplive\\.co\\.kr/m/(\\d+)\\?");
        Matcher matcher = pattern.matcher(responseBody);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalStateException("Failed to retrieve BNO. The BJ might not be streaming or an error occurred.");
        }
    }

    private static ChannelInfo getPlayerLive(String bno, String bid) throws Exception {
        String url = "https://live.sooplive.co.kr/afreeca/player_live_api.php";
        String requestBody = String.format("bid=%s&bno=%s&type=live&confirm_adult=false&player_type=html5&mode=landing&from_api=0&pwd=&stream_type=common&quality=HD", bid, bno);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "?bjid=" + bid))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Failed to retrieve player live information. Status code: " + response.statusCode());
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonObject channel = json.getAsJsonObject("CHANNEL");

        return new ChannelInfo(
                channel.get("CHDOMAIN").getAsString().toLowerCase(),
                channel.get("CHATNO").getAsString(),
                channel.get("FTK").getAsString(),
                channel.get("TITLE").getAsString(),
                channel.get("BJID").getAsString(),
                String.valueOf(channel.get("CHPT").getAsInt() + 1)
        );
    }

    private static int parseServiceCode(String header) {
        try {
            String[] headerParts = header.split("\t");
            return Integer.parseInt(headerParts[headerParts.length - 1].substring(0, 4));
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            LOGGER.log(Level.WARNING, "Error parsing service code", e);
            return -1;
        }
    }

    @Override
    public void close() {
        disconnect();
    }

    public void disconnect() {
        if (webSocket != null && isConnected) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Disconnecting");
            isConnected = false;
        }
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Interrupted while waiting for scheduler shutdown", e);
            Thread.currentThread().interrupt();
        }
    }

    private static int calculateByteSize(String string) {
        return string.getBytes().length + 6;
    }

    public static class Builder {
        private String bid;
        private String bno;
        private SSLContext sslContext;

        public Builder bid(String bid) {
            this.bid = bid;
            return this;
        }

        public Builder bno(String bno) {
            this.bno = bno;
            return this;
        }

        public Builder sslContext(SSLContext sslContext) {
            this.sslContext = sslContext;
            return this;
        }

        public SOOPChatClient build() throws Exception {
            return new SOOPChatClient(this);
        }
    }
}