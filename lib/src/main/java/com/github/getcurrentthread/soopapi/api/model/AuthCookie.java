package com.github.getcurrentthread.soopapi.api.model;

public record AuthCookie(
        String userId,
        boolean success,
        String rawResponse,
        String authTicket,
        String abroadChk,
        String abroadVod,
        String bbsTicket,
        String rdb,
        String userTicket,
        String au,
        String au3rd,
        String ausa,
        String ausb) {

    public boolean isAuthenticated() {
        return success && authTicket != null && !authTicket.isEmpty();
    }
}
