package com.github.getcurrentthread.soopapi.config;

import javax.net.ssl.SSLContext;

public class SOOPChatConfig {
    private final String bid;
    private final String bno;
    private final SSLContext sslContext;

    private SOOPChatConfig(Builder builder) {
        this.bid = builder.bid;
        this.bno = builder.bno;
        this.sslContext = builder.sslContext;
    }

    public String getBid() {
        return bid;
    }

    public String getBno() {
        return bno;
    }

    public SSLContext getSSLContext() {
        return sslContext;
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

        public SOOPChatConfig build() {
            return new SOOPChatConfig(this);
        }
    }
}
