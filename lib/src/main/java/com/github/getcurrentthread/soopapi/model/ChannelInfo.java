package com.github.getcurrentthread.soopapi.model;

public class ChannelInfo {
    private static final String SEPARATOR = "+" + "-".repeat(70) + "+";
    public final String CHDOMAIN, CHATNO, FTK, TITLE, BJID, CHPT;

    public ChannelInfo(
            String CHDOMAIN, String CHATNO, String FTK, String TITLE, String BJID, String CHPT) {
        this.CHDOMAIN = CHDOMAIN;
        this.CHATNO = CHATNO;
        this.FTK = FTK;
        this.TITLE = TITLE;
        this.BJID = BJID;
        this.CHPT = CHPT;
    }

    @Override
    public String toString() {
        return String.format(
                "%s%n  CHDOMAIN: %s%n  CHATNO: %s%n  FTK: %s%n  TITLE: %s%n  BJID: %s%n  CHPT: %s%n%s",
                SEPARATOR, CHDOMAIN, CHATNO, FTK, TITLE, BJID, CHPT, SEPARATOR);
    }
}
