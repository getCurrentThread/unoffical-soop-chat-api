package com.github.getcurrentthread.soopapi.exception;

public class SOOPChatException extends RuntimeException {
    public SOOPChatException(String message) {
        super(message);
    }

    public SOOPChatException(String message, Throwable cause) {
        super(message, cause);
    }
}
