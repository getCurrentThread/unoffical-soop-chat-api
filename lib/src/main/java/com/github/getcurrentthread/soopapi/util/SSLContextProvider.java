package com.github.getcurrentthread.soopapi.util;

import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import com.github.getcurrentthread.soopapi.exception.ConnectionException;

public class SSLContextProvider {
    private static final Logger LOGGER = Logger.getLogger(SSLContextProvider.class.getName());

    private static final class Holder {
        static final SSLContext INSTANCE = createSSLContext();
    }

    public static SSLContext getInstance() {
        return Holder.INSTANCE;
    }

    private static SSLContext createSSLContext() {
        try {
            LOGGER.info("Initializing SSLContext");
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, null, new SecureRandom());
            LOGGER.info("SSLContext initialized successfully");
            return context;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize SSL context", e);
            throw new ConnectionException("Failed to initialize SSL context", e);
        }
    }
}
