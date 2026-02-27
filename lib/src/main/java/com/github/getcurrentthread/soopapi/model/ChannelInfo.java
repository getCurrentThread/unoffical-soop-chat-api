package com.github.getcurrentthread.soopapi.model;

public record ChannelInfo(
        String CHDOMAIN, String CHATNO, String FTK, String TITLE, String BJID, String CHPT) {

    private static final String SEPARATOR = "+" + "-".repeat(70) + "+";

    @Override
    public String toString() {
        return String.format(
                "%s%n  CHDOMAIN: %s%n  CHATNO: %s%n  FTK: %s%n  TITLE: %s%n  BJID: %s%n  CHPT: %s%n%s",
                SEPARATOR, CHDOMAIN, CHATNO, FTK, TITLE, BJID, CHPT, SEPARATOR);
    }
}
