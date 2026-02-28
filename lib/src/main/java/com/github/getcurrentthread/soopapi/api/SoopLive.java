package com.github.getcurrentthread.soopapi.api;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.getcurrentthread.soopapi.api.model.AuthCookie;
import com.github.getcurrentthread.soopapi.api.model.LiveDetail;
import com.github.getcurrentthread.soopapi.exception.SOOPChatException;
import com.github.getcurrentthread.soopapi.model.ChannelInfo;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SoopLive {
    private static final Logger LOGGER = Logger.getLogger(SoopLive.class.getName());
    private static final String PLAYER_LIVE_URL =
            "https://live.sooplive.co.kr/afreeca/player_live_api.php";
    private static final String PLAY_URL = "https://play.sooplive.co.kr/";

    private final SoopHttpClient httpClient;

    public SoopLive(SoopHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CompletableFuture<String> getBno(String streamerId) {
        return httpClient
                .get(PLAY_URL + streamerId)
                .thenApply(
                        response -> {
                            if (response.statusCode() != 200) {
                                throw new SOOPChatException(
                                        "HTTP 요청 실패. 상태 코드: " + response.statusCode());
                            }

                            String body = response.body();

                            Pattern pattern =
                                    Pattern.compile(
                                            "<meta property=\"og:image\" content=\"https://liveimg\\.sooplive\\.co\\.kr/m/(\\d+)\\?");
                            Matcher matcher = pattern.matcher(body);

                            if (matcher.find()) {
                                return matcher.group(1);
                            }

                            Pattern altPattern = Pattern.compile("\"bno\"\\s*:\\s*\"?(\\d+)\"?");
                            Matcher altMatcher = altPattern.matcher(body);

                            if (altMatcher.find()) {
                                return altMatcher.group(1);
                            }

                            throw new SOOPChatException("BNO를 가져오지 못했습니다. 방송중이 아니거나 오류가 발생했습니다.");
                        });
    }

    public CompletableFuture<LiveDetail> detail(String streamerId) {
        return getBno(streamerId).thenCompose(bno -> detail(streamerId, bno));
    }

    public CompletableFuture<LiveDetail> detail(String streamerId, String bno) {
        return detail(streamerId, bno, null);
    }

    public CompletableFuture<LiveDetail> detail(
            String streamerId, String bno, AuthCookie authCookie) {
        String requestBody =
                String.format(
                        "bid=%s&bno=%s&type=live&confirm_adult=false&player_type=html5&mode=landing&from_api=0&pwd=&stream_type=common&quality=HD",
                        streamerId, bno);

        String cookieHeader = buildCookieHeader(authCookie);

        return httpClient
                .postForm(PLAYER_LIVE_URL + "?bjid=" + streamerId, requestBody, cookieHeader)
                .thenApply(
                        response -> {
                            if (response.statusCode() != 200) {
                                throw new SOOPChatException(
                                        "실시간 방송 정보를 가져오지 못했습니다. 상태 코드: " + response.statusCode());
                            }

                            try {
                                JsonObject json =
                                        JsonParser.parseString(response.body()).getAsJsonObject();

                                int result = json.has("RESULT") ? json.get("RESULT").getAsInt() : 0;

                                if (result != 1) {
                                    String reason =
                                            json.has("REASON")
                                                    ? json.get("REASON").getAsString()
                                                    : "알 수 없는 오류";
                                    throw new SOOPChatException("API 오류: " + reason);
                                }

                                if (!json.has("CHANNEL")) {
                                    throw new SOOPChatException("응답에 CHANNEL 정보가 없습니다");
                                }

                                JsonObject channel = json.getAsJsonObject("CHANNEL");

                                return new LiveDetail(
                                        channel.get("BJID").getAsString(),
                                        bno,
                                        channel.get("TITLE").getAsString(),
                                        channel.get("CHDOMAIN").getAsString().toLowerCase(),
                                        channel.get("CHATNO").getAsString(),
                                        channel.get("FTK").getAsString(),
                                        String.valueOf(channel.get("CHPT").getAsInt() + 1),
                                        result,
                                        channel.has("BPS") ? channel.get("BPS").getAsString() : "",
                                        channel.has("geo_cc")
                                                ? channel.get("geo_cc").getAsString()
                                                : "",
                                        channel.has("geo_rc")
                                                ? channel.get("geo_rc").getAsString()
                                                : "",
                                        channel.has("acpt_lang")
                                                ? channel.get("acpt_lang").getAsString()
                                                : "",
                                        channel.has("svc_lang")
                                                ? channel.get("svc_lang").getAsString()
                                                : "");
                            } catch (SOOPChatException e) {
                                throw e;
                            } catch (Exception e) {
                                LOGGER.log(Level.WARNING, "방송 정보 파싱 오류", e);
                                throw new SOOPChatException("방송 정보 파싱 실패", e);
                            }
                        });
    }

    public ChannelInfo toChannelInfo(LiveDetail detail) {
        return new ChannelInfo(
                detail.chatDomain(),
                detail.chatNo(),
                detail.ftk(),
                detail.title(),
                detail.bjId(),
                detail.chatPort(),
                detail.bps(),
                detail.geoCC(),
                detail.geoRC(),
                detail.acptLang(),
                detail.svcLang());
    }

    private String buildCookieHeader(AuthCookie authCookie) {
        if (authCookie == null || !authCookie.isAuthenticated()) {
            return null;
        }
        return String.join(
                "; ",
                "AuthTicket=" + authCookie.authTicket(),
                "_au=" + authCookie.au(),
                "UserTicket=" + authCookie.userTicket(),
                "RDB=" + authCookie.rdb());
    }
}
