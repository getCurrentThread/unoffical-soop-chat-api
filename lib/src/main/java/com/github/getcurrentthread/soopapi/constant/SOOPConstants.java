package com.github.getcurrentthread.soopapi.constant;

/**
 * SOOP 채팅 WebSocket 프로토콜에서 사용되는 프로토콜 상수.
 *
 * <p>SOOP 채팅 프로토콜은 바이너리/텍스트 프레이밍 방식을 사용하며, 각 패킷은 다음과 같이 구성됩니다:
 *
 * <pre>
 * [ESC] [command:4] [length:6] [suffix:2] [payload]
 * </pre>
 *
 * <p>페이로드는 {@link #F} (폼 피드, {@code U+000C})를 필드 구분자로 사용합니다. 페이로드 내의 구조화된
 * 메타데이터 블록은 {@link #ELEMENT_START}와 {@link #ELEMENT_END} 구분자를 사용합니다.
 * 메타데이터 내의 쿼리 파라미터는 연산자 주위에 {@link #SPACE} ({@code U+0006})를 사용합니다.
 */
public class SOOPConstants {
    /** 폼 피드 문자 ({@code U+000C}) — 패킷 페이로드의 기본 필드 구분자. */
    public static final String F = "\u000c";

    /** 효율적인 단일 문자 연산을 위한 {@code char} 타입의 폼 피드. */
    public static final char F_CHAR = '\u000c';

    /**
     * 패킷 헤더 접두사: ESC ({@code U+001B}) 뒤에 TAB ({@code U+0009}). 모든 송신 패킷은
     * 이 2바이트 시퀀스로 시작합니다.
     */
    public static final String ESC = "\u001b\t";

    /** 요소 시작 마커 ({@code U+0011}) — 구조화된 메타데이터 블록을 엽니다. */
    public static final String ELEMENT_START = "\u0011";

    /** 요소 끝 마커 ({@code U+0012}) — 구조화된 메타데이터 블록을 닫습니다. */
    public static final String ELEMENT_END = "\u0012";

    /**
     * 특수 공백 문자 ({@code U+0006}) — 메타데이터 블록 내의 로그 쿼리 파라미터에서
     * 연산자 ({@code &}, {@code =}) 주위에 사용됩니다.
     */
    public static final String SPACE = "\u0006";

    private SOOPConstants() {}
}
