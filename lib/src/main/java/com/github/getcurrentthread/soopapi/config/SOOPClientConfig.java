package com.github.getcurrentthread.soopapi.config;

import java.time.Duration;

public class SOOPClientConfig {
    private final Duration connectionTimeout;
    private final int maxRetryAttempts;

    private static final Duration DEFAULT_CONNECTION_TIMEOUT = Duration.ofSeconds(15);
    private static final int DEFAULT_MAX_RETRY_ATTEMPTS = 5;

    private SOOPClientConfig(Builder builder) {
        this.connectionTimeout = builder.connectionTimeout;
        this.maxRetryAttempts = builder.maxRetryAttempts;
    }

    public Duration getConnectionTimeout() {
        return connectionTimeout;
    }

    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }

    public static class Builder {
        private Duration connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
        private int maxRetryAttempts = DEFAULT_MAX_RETRY_ATTEMPTS;

        public Builder connectionTimeout(Duration connectionTimeout) {
            if (connectionTimeout == null) {
                throw new IllegalArgumentException("connectionTimeout must not be null");
            }
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public Builder maxRetryAttempts(int maxRetryAttempts) {
            if (maxRetryAttempts < 0) {
                throw new IllegalArgumentException("maxRetryAttempts must be >= 0");
            }
            this.maxRetryAttempts = maxRetryAttempts;
            return this;
        }

        public SOOPClientConfig build() {
            if (connectionTimeout != null && connectionTimeout.isNegative()) {
                throw new IllegalArgumentException("connectionTimeout must not be negative");
            }
            if (maxRetryAttempts < 0) {
                throw new IllegalArgumentException("maxRetryAttempts must be >= 0");
            }
            return new SOOPClientConfig(this);
        }
    }
}
