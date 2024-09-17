package com.github.getcurrentthread.afreecatvapi.util;

import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class DefaultSSLContext {
    private static SSLContext instance;

    private DefaultSSLContext() {}

    public static synchronized SSLContext getInstance() throws Exception {
        if (instance == null) {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }};

            instance = SSLContext.getInstance("TLS");
            instance.init(null, trustAllCerts, new java.security.SecureRandom());
        }
        return instance;
    }
}
