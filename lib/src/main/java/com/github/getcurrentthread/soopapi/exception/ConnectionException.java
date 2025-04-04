package com.github.getcurrentthread.soopapi.exception;

/** 서버 연결 문제에 대한 사용자 정의 예외입니다. 초기 연결, 메시지 전송 또는 읽기 중 발생하는 연결 관련 오류를 나타냅니다. */
public class ConnectionException extends RuntimeException {

    /**
     * 지정된 메시지로 새 ConnectionException을 구성합니다.
     *
     * @param message 예외 메시지
     */
    public ConnectionException(String message) {
        super(message);
    }

    /**
     * 지정된 메시지와 원인으로 새 ConnectionException을 구성합니다.
     *
     * @param message 예외 메시지
     * @param cause 원인 (null 허용)
     */
    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
