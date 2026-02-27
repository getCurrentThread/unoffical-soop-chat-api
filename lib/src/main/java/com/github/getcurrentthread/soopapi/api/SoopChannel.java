package com.github.getcurrentthread.soopapi.api;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.getcurrentthread.soopapi.api.model.StationInfo;
import com.github.getcurrentthread.soopapi.exception.SOOPChatException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SoopChannel {
    private static final Logger LOGGER = Logger.getLogger(SoopChannel.class.getName());
    private static final String STATION_URL = "https://chapi.sooplive.co.kr/api/%s/station";

    private final SoopHttpClient httpClient;

    public SoopChannel(SoopHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CompletableFuture<StationInfo> station(String streamerId) {
        String url = String.format(STATION_URL, streamerId);

        return httpClient
                .get(url)
                .thenApply(
                        response -> {
                            if (response.statusCode() != 200) {
                                throw new SOOPChatException(
                                        "스테이션 정보를 가져오지 못했습니다. 상태 코드: " + response.statusCode());
                            }

                            try {
                                JsonObject json =
                                        JsonParser.parseString(response.body()).getAsJsonObject();

                                String userId =
                                        json.has("user_id")
                                                ? json.get("user_id").getAsString()
                                                : streamerId;
                                String userNickname =
                                        json.has("user_nick")
                                                ? json.get("user_nick").getAsString()
                                                : "";
                                long stationNo =
                                        json.has("station_no")
                                                ? json.get("station_no").getAsLong()
                                                : 0;
                                String stationName =
                                        json.has("station_name")
                                                ? json.get("station_name").getAsString()
                                                : "";
                                String stationTitle =
                                        json.has("station_title")
                                                ? json.get("station_title").getAsString()
                                                : "";
                                boolean isLive =
                                        json.has("is_live") && json.get("is_live").getAsInt() == 1;
                                int totalFollowers =
                                        json.has("total_followers")
                                                ? json.get("total_followers").getAsInt()
                                                : 0;

                                return new StationInfo(
                                        userId,
                                        userNickname,
                                        stationNo,
                                        stationName,
                                        stationTitle,
                                        isLive,
                                        totalFollowers);
                            } catch (Exception e) {
                                LOGGER.log(Level.WARNING, "스테이션 정보 파싱 오류", e);
                                throw new SOOPChatException("스테이션 정보 파싱 실패", e);
                            }
                        });
    }
}
