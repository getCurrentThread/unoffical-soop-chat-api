package com.github.getcurrentthread.soopapi.util;

import com.github.getcurrentthread.soopapi.exception.SOOPChatException;
import com.github.getcurrentthread.soopapi.model.ChannelInfo;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SOOPChatUtils {
    private static final Logger LOGGER = Logger.getLogger(SOOPChatUtils.class.getName());
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36";

    private SOOPChatUtils() {
    }

    public static String getBnoFromBid(String bid) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://play.sooplive.co.kr/" + bid))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            Pattern pattern = Pattern.compile("<meta property=\"og:image\" content=\"https://liveimg\\.sooplive\\.co\\.kr/m/(\\d+)\\?");
            Matcher matcher = pattern.matcher(responseBody);

            if (matcher.find()) {
                return matcher.group(1);
            } else {
                throw new SOOPChatException("Failed to retrieve BNO. The BJ might not be streaming or an error occurred.");
            }
        } catch (Exception e) {
            throw new SOOPChatException("Error occurred while getting BNO from BID", e);
        }
    }

    public static ChannelInfo getPlayerLive(String bno, String bid) {
        String url = "https://live.sooplive.co.kr/afreeca/player_live_api.php";
        String requestBody = String.format("bid=%s&bno=%s&type=live&confirm_adult=false&player_type=html5&mode=landing&from_api=0&pwd=&stream_type=common&quality=HD", bid, bno);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "?bjid=" + bid))
                .header("User-Agent", USER_AGENT)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new SOOPChatException("Failed to retrieve player live information. Status code: " + response.statusCode());
            }

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonObject channel = json.getAsJsonObject("CHANNEL");

            return new ChannelInfo(
                    channel.get("CHDOMAIN").getAsString().toLowerCase(),
                    channel.get("CHATNO").getAsString(),
                    channel.get("FTK").getAsString(),
                    channel.get("TITLE").getAsString(),
                    channel.get("BJID").getAsString(),
                    String.valueOf(channel.get("CHPT").getAsInt() + 1)
            );
        } catch (Exception e) {
            throw new SOOPChatException("Error occurred while getting player live information", e);
        }
    }

    public static int calculateByteSize(String string) {
        return string.getBytes().length + 6;
    }

    public static int parseServiceCode(String header) {
        try {
            String[] headerParts = header.split("\t");
            if (headerParts.length < 2) {
                return -1;
            }
            String lastPart = headerParts[headerParts.length - 1];
            if (lastPart.length() < 4) {
                LOGGER.warning("Last header part is too short: " + lastPart);
                return -1;
            }
            return Integer.parseInt(lastPart.substring(0, 4));
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            LOGGER.log(Level.WARNING, "Error parsing service code", e);
            return -1;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unexpected error parsing service code", e);
            return -1;
        }
    }
}