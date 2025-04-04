package com.github.getcurrentthread.soopapi.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.getcurrentthread.soopapi.exception.ConnectionException;
import com.github.getcurrentthread.soopapi.exception.SOOPChatException;
import com.github.getcurrentthread.soopapi.model.ChannelInfo;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SOOPChatUtils {
    private static final Logger LOGGER = Logger.getLogger(SOOPChatUtils.class.getName());
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";
    private static final int CONNECTION_TIMEOUT_SECONDS = 15;

    private SOOPChatUtils() {}

    /**
     * 방송인 ID(BID)로부터 방송 번호(BNO)를 가져옵니다.
     *
     * @param bid 방송인 ID
     * @return 방송 번호
     * @throws SOOPChatException API 요청 중 오류가 발생한 경우
     */
    public static String getBnoFromBid(String bid) {
        // HTTP 클라이언트 생성 - 타임아웃 설정 추가
        HttpClient client =
                HttpClient.newBuilder()
                        .connectTimeout(java.time.Duration.ofSeconds(CONNECTION_TIMEOUT_SECONDS))
                        .build();

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create("https://play.sooplive.co.kr/" + bid))
                        .GET()
                        .header("User-Agent", USER_AGENT)
                        .build();

        try {
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new SOOPChatException("HTTP 요청 실패. 상태 코드: " + response.statusCode());
            }

            String responseBody = response.body();

            Pattern pattern =
                    Pattern.compile(
                            "<meta property=\"og:image\" content=\"https://liveimg\\.sooplive\\.co\\.kr/m/(\\d+)\\?");
            Matcher matcher = pattern.matcher(responseBody);

            if (matcher.find()) {
                return matcher.group(1);
            } else {
                // 다른 패턴 시도 (SOOP 사이트가 변경되었을 수 있음)
                Pattern altPattern = Pattern.compile("\"bno\"\\s*:\\s*\"?(\\d+)\"?");
                Matcher altMatcher = altPattern.matcher(responseBody);

                if (altMatcher.find()) {
                    return altMatcher.group(1);
                }

                throw new SOOPChatException("BNO를 가져오지 못했습니다. 방송중이 아니거나 오류가 발생했습니다.");
            }
        } catch (SOOPChatException e) {
            throw e; // 사용자 정의 예외는 그대로 전달
        } catch (Exception e) {
            throw new SOOPChatException("BID로부터 BNO를 가져오는 중 오류 발생", e);
        }
    }

    /**
     * 방송 정보를 가져옵니다.
     *
     * @param bno 방송 번호
     * @param bid 방송인 ID
     * @return 채널 정보
     * @throws SOOPChatException API 요청 중 오류가 발생한 경우
     * @throws ConnectionException 응답 파싱 중 오류가 발생한 경우
     */
    public static ChannelInfo getPlayerLive(String bno, String bid) {
        String url = "https://live.sooplive.co.kr/afreeca/player_live_api.php";
        String requestBody =
                String.format(
                        "bid=%s&bno=%s&type=live&confirm_adult=false&player_type=html5&mode=landing&from_api=0&pwd=&stream_type=common&quality=HD",
                        bid, bno);

        // HTTP 클라이언트 생성 - 타임아웃 설정 추가
        HttpClient client =
                HttpClient.newBuilder()
                        .connectTimeout(java.time.Duration.ofSeconds(CONNECTION_TIMEOUT_SECONDS))
                        .build();

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url + "?bjid=" + bid))
                        .header("User-Agent", USER_AGENT)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

        try {
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new SOOPChatException(
                        "실시간 방송 정보를 가져오지 못했습니다. 상태 코드: " + response.statusCode());
            }

            // 응답 로깅
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("API 응답: " + response.body());
            }

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

            // 오류 응답 확인
            if (json.has("RESULT") && json.get("RESULT").getAsInt() != 1) {
                String errorMessage = "API 오류: ";
                if (json.has("REASON")) {
                    errorMessage += json.get("REASON").getAsString();
                } else {
                    errorMessage += "알 수 없는 오류";
                }
                throw new SOOPChatException(errorMessage);
            }

            if (!json.has("CHANNEL")) {
                throw new SOOPChatException("응답에 CHANNEL 정보가 없습니다");
            }

            JsonObject channel = json.getAsJsonObject("CHANNEL");

            // 필수 필드 확인
            validateField(channel, "CHDOMAIN");
            validateField(channel, "CHATNO");
            validateField(channel, "FTK");
            validateField(channel, "TITLE");
            validateField(channel, "BJID");
            validateField(channel, "CHPT");

            return new ChannelInfo(
                    channel.get("CHDOMAIN").getAsString().toLowerCase(),
                    channel.get("CHATNO").getAsString(),
                    channel.get("FTK").getAsString(),
                    channel.get("TITLE").getAsString(),
                    channel.get("BJID").getAsString(),
                    String.valueOf(channel.get("CHPT").getAsInt() + 1));
        } catch (SOOPChatException e) {
            throw e; // 사용자 정의 예외는 그대로 전달
        } catch (Exception e) {
            throw new SOOPChatException("실시간 방송 정보를 가져오는 중 오류 발생", e);
        }
    }

    /**
     * JSON 객체에서 필드 존재 여부를 확인합니다.
     *
     * @param json JSON 객체
     * @param fieldName 확인할 필드 이름
     * @throws ConnectionException 필드가 없거나 null인 경우
     */
    private static void validateField(JsonObject json, String fieldName)
            throws ConnectionException {
        if (!json.has(fieldName) || json.get(fieldName).isJsonNull()) {
            throw new ConnectionException("필수 필드가 없습니다: " + fieldName);
        }
    }

    /**
     * 바이트 크기를 계산합니다.
     *
     * @param string 크기를 계산할 문자열
     * @return 바이트 크기
     */
    public static int calculateByteSize(String string) {
        return string.getBytes().length + 6;
    }

    /**
     * 서비스 코드를 파싱합니다.
     *
     * @param header 헤더 문자열
     * @return 서비스 코드
     */
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
