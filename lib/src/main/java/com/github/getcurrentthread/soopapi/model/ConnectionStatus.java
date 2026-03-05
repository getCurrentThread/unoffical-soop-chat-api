package com.github.getcurrentthread.soopapi.model;

/**
 * SOOP 채팅 세션의 현재 연결 상태를 나타냅니다.
 *
 * @param connected 현재 연결이 활성 상태인지 여부
 * @param reconnecting 재연결 시도가 진행 중인지 여부
 * @param retryCount 지금까지 수행된 재시도 횟수
 */
public record ConnectionStatus(boolean connected, boolean reconnecting, int retryCount) {}
