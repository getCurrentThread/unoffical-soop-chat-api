package com.github.getcurrentthread.soopapi.util;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

public class SSLContextProvider {
    private static SSLContext instance;

    private SSLContextProvider() {
    }

    public static synchronized SSLContext getInstance() {
        if (instance == null) {
            try {
                TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }};

                instance = SSLContext.getInstance("TLS");
                instance.init(null, trustAllCerts, new java.security.SecureRandom());
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize SSL context", e);
            }
        }
        return instance;
    }
}