package com.github.getcurrentthread.soopapi.util;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SSLContextProvider {
    private static final Logger LOGGER = Logger.getLogger(SSLContextProvider.class.getName());
    private static volatile SSLContext instance;

    private SSLContextProvider() {}

    public static SSLContext getInstance() {
        if (instance == null) {
            synchronized (SSLContextProvider.class) {
                if (instance == null) {
                    try {
                        LOGGER.info("Initializing SSLContext");
                        TrustManager[] trustAllCerts =
                                new TrustManager[] {
                                    new X509TrustManager() {
                                        @Override
                                        public X509Certificate[] getAcceptedIssuers() {
                                            return new X509Certificate[0];
                                        }

                                        @Override
                                        public void checkClientTrusted(
                                                X509Certificate[] certs, String authType) {}

                                        @Override
                                        public void checkServerTrusted(
                                                X509Certificate[] certs, String authType) {}
                                    }
                                };

                        SSLContext context = SSLContext.getInstance("TLS");
                        context.init(null, trustAllCerts, new SecureRandom());
                        instance = context;
                        LOGGER.info("SSLContext initialized successfully");
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Failed to initialize SSL context", e);
                        throw new RuntimeException("Failed to initialize SSL context", e);
                    }
                }
            }
        }
        return instance;
    }
}
