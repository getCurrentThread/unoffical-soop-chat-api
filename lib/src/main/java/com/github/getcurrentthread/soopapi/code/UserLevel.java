package com.github.getcurrentthread.soopapi.code;

import java.util.EnumSet;
import java.util.Set;

import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

/**
 * 파싱된 사용자 레벨 — 주/보조 두 비트 플래그 그룹의 컨테이너.
 *
 * <p>SOOP 입장 플래그는 {@code "primary|secondary"} 형식의 문자열로 전달됩니다(예: {@code "81952|32768"}). 두 정수는 서로 다른
 * 비트 의미를 가지므로 각각 {@link UserFlag}, {@link UserFlag2}로 분해합니다.
 *
 * @param primary 주 그룹 플래그 집합
 * @param secondary 보조 그룹 플래그 집합
 */
public record UserLevel(Set<UserFlag> primary, Set<UserFlag2> secondary) {

    /** 빈/해제 상태. {@code null}·빈 문자열·파싱 불가 입력에 대해 반환됩니다. */
    public static final UserLevel EMPTY =
            new UserLevel(EnumSet.noneOf(UserFlag.class), EnumSet.noneOf(UserFlag2.class));

    /**
     * {@code "primary|secondary"} 플래그 문자열을 파싱합니다.
     *
     * <p>방어적으로 동작하며 절대 예외를 던지지 않습니다.
     *
     * <ul>
     *   <li>{@code null}/빈 문자열 → {@link #EMPTY}
     *   <li>{@code |} 없음 → 전체를 주 그룹으로, 보조는 빈 집합
     *   <li>숫자가 아닌 쪽 → 해당 그룹은 빈 집합({@link SOOPChatUtils#safeParseInt}가 0 반환)
     * </ul>
     *
     * @param flag 입장 플래그 문자열
     * @return 파싱된 {@link UserLevel}
     */
    public static UserLevel parse(String flag) {
        if (flag == null || flag.isEmpty()) {
            return EMPTY;
        }
        int pipe = flag.indexOf('|');
        if (pipe < 0) {
            int p = SOOPChatUtils.safeParseInt(flag.trim(), 0);
            return new UserLevel(UserFlag.fromMask(p), EnumSet.noneOf(UserFlag2.class));
        }
        int p = SOOPChatUtils.safeParseInt(flag.substring(0, pipe).trim(), 0);
        int s = SOOPChatUtils.safeParseInt(flag.substring(pipe + 1).trim(), 0);
        return new UserLevel(UserFlag.fromMask(p), UserFlag2.fromMask(s));
    }

    /**
     * 주 그룹에 해당 플래그가 있는지 확인합니다.
     *
     * @param flag 확인할 주 그룹 플래그
     * @return 포함 여부
     */
    public boolean has(UserFlag flag) {
        return primary.contains(flag);
    }

    /**
     * 보조 그룹에 해당 플래그가 있는지 확인합니다.
     *
     * @param flag 확인할 보조 그룹 플래그
     * @return 포함 여부
     */
    public boolean has(UserFlag2 flag) {
        return secondary.contains(flag);
    }
}
