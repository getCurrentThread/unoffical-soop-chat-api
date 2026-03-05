package com.github.getcurrentthread.soopapi;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.getcurrentthread.soopapi.api.SOOPAuth;
import com.github.getcurrentthread.soopapi.api.SOOPChannel;
import com.github.getcurrentthread.soopapi.api.SOOPHttpClient;
import com.github.getcurrentthread.soopapi.api.SOOPLive;
import com.github.getcurrentthread.soopapi.client.SOOPChatClient;
import com.github.getcurrentthread.soopapi.config.SOOPChatConfig;
import com.github.getcurrentthread.soopapi.config.SOOPClientConfig;

public class SOOPClient implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(SOOPClient.class.getName());

    private final SOOPAuth auth;
    private final SOOPLive live;
    private final SOOPChannel channel;
    private final SOOPHttpClient httpClient;
    private final List<SOOPChatClient> chatClients = new CopyOnWriteArrayList<>();

    public SOOPClient() {
        this(new SOOPClientConfig.Builder().build());
    }

    public SOOPClient(SOOPClientConfig config) {
        this.httpClient = new SOOPHttpClient(config.getConnectionTimeout());
        this.auth = new SOOPAuth(httpClient);
        this.live = new SOOPLive(httpClient);
        this.channel = new SOOPChannel(httpClient);
    }

    public SOOPAuth auth() {
        return auth;
    }

    public SOOPLive live() {
        return live;
    }

    public SOOPChannel channel() {
        return channel;
    }

    public SOOPChatClient chat(String streamerId) {
        SOOPChatConfig chatConfig = new SOOPChatConfig.Builder().bid(streamerId).build();
        return chat(chatConfig);
    }

    public SOOPChatClient chat(SOOPChatConfig config) {
        SOOPChatClient client = new SOOPChatClient(config);
        chatClients.add(client);
        return client;
    }

    @Override
    public void close() {
        for (SOOPChatClient client : chatClients) {
            try {
                client.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error closing chat client", e);
            }
        }
        chatClients.clear();
        httpClient.close();
    }
}
