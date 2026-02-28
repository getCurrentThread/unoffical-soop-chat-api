package com.github.getcurrentthread.soopapi.exception;

/** 인증이 필요한 작업을 미인증 상태에서 시도할 때 발생하는 예외입니다. */
public class AuthenticationException extends SOOPChatException {

    /**
     * 지정된 메시지로 새 AuthenticationException을 구성합니다.
     *
     * @param message 예외 메시지
     */
    public AuthenticationException(String message) {
        super(message);
    }

    /**
     * 지정된 메시지와 원인으로 새 AuthenticationException을 구성합니다.
     *
     * @param message 예외 메시지
     * @param cause 원인 (null 허용)
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
