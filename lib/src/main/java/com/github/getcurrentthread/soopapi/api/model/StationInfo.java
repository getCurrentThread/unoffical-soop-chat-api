package com.github.getcurrentthread.soopapi.api.model;

public record StationInfo(
        String userId,
        String userNickname,
        long stationNo,
        String stationName,
        String stationTitle,
        boolean isLive,
        int totalFollowers) {}
