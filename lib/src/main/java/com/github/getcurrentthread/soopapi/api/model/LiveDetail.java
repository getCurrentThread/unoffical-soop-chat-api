package com.github.getcurrentthread.soopapi.api.model;

public record LiveDetail(
        String bjId,
        String bno,
        String title,
        String chatDomain,
        String chatNo,
        String ftk,
        String chatPort,
        int result,
        String bps,
        String geoCC,
        String geoRC,
        String acptLang,
        String svcLang) {

    public LiveDetail(
            String bjId,
            String bno,
            String title,
            String chatDomain,
            String chatNo,
            String ftk,
            String chatPort,
            int result) {
        this(bjId, bno, title, chatDomain, chatNo, ftk, chatPort, result, "", "", "", "", "");
    }
}
