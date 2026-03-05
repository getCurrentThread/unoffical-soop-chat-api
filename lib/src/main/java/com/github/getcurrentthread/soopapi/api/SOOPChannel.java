package com.github.getcurrentthread.soopapi.api;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.getcurrentthread.soopapi.api.model.StationInfo;
import com.github.getcurrentthread.soopapi.exception.SOOPChatException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SOOPChannel {
    private static final Logger LOGGER = Logger.getLogger(SOOPChannel.class.getName());
    private static final String STATION_URL = "https://chapi.sooplive.co.kr/api/%s/station";

    private final SOOPHttpClient httpClient;

    public SOOPChannel(SOOPHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CompletableFuture<StationInfo> station(String streamerId) {
        String url =
                String.format(STATION_URL, URLEncoder.encode(streamerId, StandardCharsets.UTF_8));

        return httpClient
                .get(url)
                .thenApply(
                        response -> {
                            if (response.statusCode() != 200) {
                                throw new SOOPChatException(
                                        "Failed to retrieve station info. Status code: "
                                                + response.statusCode());
                            }

                            try {
                                JsonObject json =
                                        JsonParser.parseString(response.body()).getAsJsonObject();

                                return new StationInfo(
                                        getString(json, "user_id", streamerId),
                                        getString(json, "user_nick", ""),
                                        getLong(json, "station_no", 0),
                                        getString(json, "station_name", ""),
                                        getString(json, "station_title", ""),
                                        getInt(json, "is_live", 0) == 1,
                                        getInt(json, "total_followers", 0));
                            } catch (Exception e) {
                                LOGGER.log(Level.WARNING, "Error parsing station info", e);
                                throw new SOOPChatException("Failed to parse station info", e);
                            }
                        });
    }

    private static String getString(JsonObject json, String key, String defaultValue) {
        return json.has(key) && !json.get(key).isJsonNull()
                ? json.get(key).getAsString()
                : defaultValue;
    }

    private static int getInt(JsonObject json, String key, int defaultValue) {
        return json.has(key) && !json.get(key).isJsonNull()
                ? json.get(key).getAsInt()
                : defaultValue;
    }

    private static long getLong(JsonObject json, String key, long defaultValue) {
        return json.has(key) && !json.get(key).isJsonNull()
                ? json.get(key).getAsLong()
                : defaultValue;
    }
}
