package com.github.getcurrentthread.soopapi.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SOOPChatUtils {
    private static final Logger LOGGER = Logger.getLogger(SOOPChatUtils.class.getName());

    private SOOPChatUtils() {}

    /**
     * 바이트 크기를 계산합니다.
     *
     * @param string 크기를 계산할 문자열
     * @return 바이트 크기
     */
    public static int calculateByteSize(String string) {
        return utf8ByteLength(string) + 6;
    }

    /** 바이트 배열 할당 없이 UTF-8 바이트 길이를 계산합니다. */
    public static int utf8ByteLength(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c <= 0x7F) {
                count++;
            } else if (c <= 0x7FF) {
                count += 2;
            } else if (Character.isHighSurrogate(c)) {
                count += 4;
                i++;
            } else {
                count += 3;
            }
        }
        return count;
    }

    /**
     * 문자열을 안전하게 int로 파싱합니다. 파싱 실패 시 기본값을 반환합니다.
     *
     * @param value 파싱할 문자열
     * @param defaultValue 파싱 실패 시 반환할 기본값
     * @return 파싱된 int 값 또는 기본값
     */
    public static int safeParseInt(String value, int defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 문자열을 안전하게 long으로 파싱합니다. 파싱 실패 시 기본값을 반환합니다.
     *
     * @param value 파싱할 문자열
     * @param defaultValue 파싱 실패 시 반환할 기본값
     * @return 파싱된 long 값 또는 기본값
     */
    public static long safeParseLong(String value, long defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * CompletionException 체인을 풀어서 근본 원인을 반환합니다.
     *
     * @param throwable 풀어볼 예외
     * @return 근본 원인 예외
     */
    public static Throwable unwrapCompletionException(Throwable throwable) {
        Throwable cause = throwable;
        while (cause instanceof java.util.concurrent.CompletionException
                && cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

    /**
     * 서비스 코드를 파싱합니다.
     *
     * @param header 헤더 문자열
     * @return 서비스 코드
     */
    public static int parseServiceCode(String header) {
        try {
            String[] headerParts = header.split("\t");
            if (headerParts.length < 2) {
                return -1;
            }
            String lastPart = headerParts[headerParts.length - 1];
            if (lastPart.length() < 4) {
                LOGGER.warning("Last header part is too short: " + lastPart);
                return -1;
            }
            return Integer.parseInt(lastPart.substring(0, 4));
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            LOGGER.log(Level.WARNING, "Error parsing service code", e);
            return -1;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unexpected error parsing service code", e);
            return -1;
        }
    }
}
