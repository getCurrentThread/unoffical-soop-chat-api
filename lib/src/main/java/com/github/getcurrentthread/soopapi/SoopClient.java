package com.github.getcurrentthread.soopapi;

import com.github.getcurrentthread.soopapi.api.SoopAuth;
import com.github.getcurrentthread.soopapi.api.SoopChannel;
import com.github.getcurrentthread.soopapi.api.SoopHttpClient;
import com.github.getcurrentthread.soopapi.api.SoopLive;
import com.github.getcurrentthread.soopapi.client.SOOPChatClient;
import com.github.getcurrentthread.soopapi.config.SOOPChatConfig;
import com.github.getcurrentthread.soopapi.config.SoopClientConfig;

public class SoopClient implements AutoCloseable {
    public final SoopAuth auth;
    public final SoopLive live;
    public final SoopChannel channel;

    private final SoopHttpClient httpClient;

    public SoopClient() {
        this(new SoopClientConfig.Builder().build());
    }

    public SoopClient(SoopClientConfig config) {
        this.httpClient = new SoopHttpClient();
        this.auth = new SoopAuth(httpClient);
        this.live = new SoopLive(httpClient);
        this.channel = new SoopChannel(httpClient);
    }

    public SOOPChatClient chat(String streamerId) {
        SOOPChatConfig chatConfig = new SOOPChatConfig.Builder().bid(streamerId).build();
        return new SOOPChatClient(chatConfig);
    }

    public SOOPChatClient chat(SOOPChatConfig config) {
        return new SOOPChatClient(config);
    }

    @Override
    public void close() {
        // SoopHttpClient uses JDK HttpClient which is managed by the JVM
    }
}
