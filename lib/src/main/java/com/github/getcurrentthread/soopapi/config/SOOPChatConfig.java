package com.github.getcurrentthread.soopapi.config;

import java.time.Duration;

import javax.net.ssl.SSLContext;

public class SOOPChatConfig {
    private final String bid;
    private final String bno;
    private final SSLContext sslContext;
    private final Duration connectionTimeout;
    private final int maxRetryAttempts;

    private static final Duration DEFAULT_CONNECTION_TIMEOUT = Duration.ofSeconds(30);
    private static final int DEFAULT_MAX_RETRY_ATTEMPTS = 5;

    private SOOPChatConfig(Builder builder) {
        this.bid = builder.bid;
        this.bno = builder.bno;
        this.sslContext = builder.sslContext;
        this.connectionTimeout = builder.connectionTimeout;
        this.maxRetryAttempts = builder.maxRetryAttempts;
    }

    /**
     * 방송인 ID를 반환합니다.
     *
     * @return 방송인 ID
     */
    public String getBid() {
        return bid;
    }

    /**
     * 방송 번호를 반환합니다.
     *
     * @return 방송 번호 (null일 수 있음)
     */
    public String getBno() {
        return bno;
    }

    /**
     * SSL Context를 반환합니다.
     *
     * @return SSL Context (null일 수 있음)
     */
    public SSLContext getSSLContext() {
        return sslContext;
    }

    /**
     * 연결 타임아웃을 반환합니다.
     *
     * @return 연결 타임아웃
     */
    public Duration getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * 최대 재시도 횟수를 반환합니다.
     *
     * @return 최대 재시도 횟수
     */
    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }

    /** SOOPChatConfig 빌더 클래스 */
    public static class Builder {
        private String bid;
        private String bno;
        private SSLContext sslContext;
        private Duration connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
        private int maxRetryAttempts = DEFAULT_MAX_RETRY_ATTEMPTS;

        /**
         * 방송인 ID를 설정합니다.
         *
         * @param bid 방송인 ID
         * @return 빌더 인스턴스
         */
        public Builder bid(String bid) {
            this.bid = bid;
            return this;
        }

        /**
         * 방송 번호를 설정합니다.
         *
         * @param bno 방송 번호
         * @return 빌더 인스턴스
         */
        public Builder bno(String bno) {
            this.bno = bno;
            return this;
        }

        /**
         * SSL Context를 설정합니다.
         *
         * @param sslContext SSL Context
         * @return 빌더 인스턴스
         */
        public Builder sslContext(SSLContext sslContext) {
            this.sslContext = sslContext;
            return this;
        }

        /**
         * 연결 타임아웃을 설정합니다.
         *
         * @param connectionTimeout 연결 타임아웃
         * @return 빌더 인스턴스
         */
        public Builder connectionTimeout(Duration connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        /**
         * 최대 재시도 횟수를 설정합니다.
         *
         * @param maxRetryAttempts 최대 재시도 횟수
         * @return 빌더 인스턴스
         */
        public Builder maxRetryAttempts(int maxRetryAttempts) {
            this.maxRetryAttempts = maxRetryAttempts;
            return this;
        }

        /**
         * SOOPChatConfig 인스턴스를 생성합니다.
         *
         * @return 구성된 SOOPChatConfig 인스턴스
         */
        public SOOPChatConfig build() {
            return new SOOPChatConfig(this);
        }
    }
}
