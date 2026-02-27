package com.github.getcurrentthread.soopapi.config;

import java.time.Duration;

public class SoopClientConfig {
    private final Duration connectionTimeout;
    private final int maxRetryAttempts;

    private static final Duration DEFAULT_CONNECTION_TIMEOUT = Duration.ofSeconds(15);
    private static final int DEFAULT_MAX_RETRY_ATTEMPTS = 5;

    private SoopClientConfig(Builder builder) {
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
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public Builder maxRetryAttempts(int maxRetryAttempts) {
            this.maxRetryAttempts = maxRetryAttempts;
            return this;
        }

        public SoopClientConfig build() {
            return new SoopClientConfig(this);
        }
    }
}
