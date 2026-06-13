package com.github.getcurrentthread.soopapi.code;

import java.util.EnumSet;
import java.util.Set;

/**
 * 사용자 레벨 비트 플래그 — <b>보조(secondary) 그룹</b>.
 *
 * <p>입장 플래그 문자열 {@code "primary|secondary"} 중 두 번째 정수를 분해할 때 사용합니다.
 *
 * <p>주 그룹({@link UserFlag})과 비트 값이 겹치므로 각 정수는 반드시 자신의 그룹에 해당하는 {@code enum}으로만 분해해야 합니다.
 *
 * @see UserLevel
 */
public enum UserFlag2 {
    GLOBAL_PC(1),
    CLAN(2),
    TOPCLAN(4),
    TOP20(8),
    GAMEGOD(16),
    GAMEIMO(32),
    NOSUPERCHAT(64),
    NORECVCHAT(128),
    FLASH(256),
    LGGAME(512),
    EMPLOYEE(1024),
    CLEANATI(2048),
    POLICE(4096),
    ADMINCHAT(8192),
    PC(16384),
    SPECIFY(32768),
    NEW_STUDIO(65536),
    HTML5(1 << 17),
    FOLLOWER_PERIOD_6(1 << 18),
    FOLLOWER_PERIOD_12(1 << 19),
    FOLLOWER_PERIOD_24(1 << 20),
    /**
     * 값 {@code 4194304}({@code 1 << 22}). 공식 코드표는 {@code 1 << 24}로 주석했으나 실제 값은 {@code 4194304}입니다.
     */
    FOLLOWER_PERIOD_36(1 << 22),
    FOLLOWER_PERIOD_3(1 << 23),
    /** 공식 코드표의 {@code hide_sex}. Java enum 관례에 따라 대문자 표기. */
    HIDE_SEX(1 << 25);

    private final int bit;

    UserFlag2(int bit) {
        this.bit = bit;
    }

    public int getBit() {
        return bit;
    }

    /**
     * 보조 그룹 마스크를 켜져 있는 플래그 집합으로 분해합니다. 알 수 없는 비트는 무시됩니다(알려진 플래그만 분해).
     *
     * @param mask 보조 그룹 정수
     * @return 마스크에 포함된 플래그 집합(선언 순서)
     */
    public static Set<UserFlag2> fromMask(int mask) {
        EnumSet<UserFlag2> set = EnumSet.noneOf(UserFlag2.class);
        for (UserFlag2 f : values()) {
            if ((mask & f.bit) == f.bit) {
                set.add(f);
            }
        }
        return set;
    }

    /**
     * 플래그 집합을 정수 마스크로 합칩니다. {@link #fromMask(int)}의 역연산.
     *
     * @param flags 합칠 플래그 집합
     * @return OR 결합된 정수 마스크
     */
    public static int toMask(Set<UserFlag2> flags) {
        int mask = 0;
        for (UserFlag2 f : flags) {
            mask |= f.bit;
        }
        return mask;
    }
}
