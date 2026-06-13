package com.github.getcurrentthread.soopapi.code;

import java.util.EnumSet;
import java.util.Set;

/**
 * 사용자 레벨 비트 플래그 — <b>주(primary) 그룹</b>.
 *
 * <p>입장 플래그 문자열 {@code "primary|secondary"} 중 첫 번째 정수를 분해할 때 사용합니다.
 *
 * <p>보조 그룹({@link UserFlag2})과 비트 값이 겹치므로(예: {@code 16} = {@link #GUEST} vs {@code GAMEGOD}) 각 정수는
 * 반드시 자신의 그룹에 해당하는 {@code enum}으로만 분해해야 합니다.
 *
 * @see UserLevel
 */
public enum UserFlag {
    ADMIN(1),
    HIDDEN(2),
    BJ(4),
    DUMB(8),
    GUEST(16),
    FANCLUB(32),
    AUTOMANAGER(64),
    MANAGERLIST(128),
    MANAGER(256),
    FEMALE(512),
    AUTODUMB(1024),
    DUMB_BLIND(2048),
    DOBAE_BLIND(4096),
    EXITUSER(8192),
    MOBILE(16384),
    TOPFAN(32768),
    REALNAME(65536),
    NODIRECT(1 << 17),
    GLOBAL_APP(1 << 18),
    QUICKVIEW(1 << 19),
    SPTR_STICKER(1 << 20),
    CHROMECAST(1 << 21),
    DOBAE_BLIND2(1 << 24),
    FOLLOWER(1 << 28),
    NOTIVODBALLOON(1 << 30),
    /** {@code 1 << 31} — signed int로는 음수({@code 0x80000000})이지만 비트 마스킹에는 영향이 없습니다. */
    NOTITOPFAN(1 << 31);

    private final int bit;

    UserFlag(int bit) {
        this.bit = bit;
    }

    public int getBit() {
        return bit;
    }

    /**
     * 주 그룹 마스크를 켜져 있는 플래그 집합으로 분해합니다.
     *
     * <p>부호 비트({@link #NOTITOPFAN}, {@code 1 << 31})도 {@code (mask & bit) == bit} 검사로 올바르게 처리됩니다. 알
     * 수 없는 비트는 무시됩니다(알려진 플래그만 분해).
     *
     * @param mask 주 그룹 정수
     * @return 마스크에 포함된 플래그 집합(선언 순서)
     */
    public static Set<UserFlag> fromMask(int mask) {
        EnumSet<UserFlag> set = EnumSet.noneOf(UserFlag.class);
        for (UserFlag f : values()) {
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
    public static int toMask(Set<UserFlag> flags) {
        int mask = 0;
        for (UserFlag f : flags) {
            mask |= f.bit;
        }
        return mask;
    }
}
