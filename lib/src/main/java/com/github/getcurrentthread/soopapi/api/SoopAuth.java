package com.github.getcurrentthread.soopapi.api;

import java.net.HttpCookie;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.getcurrentthread.soopapi.api.model.AuthCookie;
import com.github.getcurrentthread.soopapi.exception.SOOPChatException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SoopAuth {
    private static final Logger LOGGER = Logger.getLogger(SoopAuth.class.getName());
    private static final String LOGIN_URL = "https://login.sooplive.co.kr/app/LoginAction.php";

    private final SoopHttpClient httpClient;

    public SoopAuth(SoopHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CompletableFuture<AuthCookie> signIn(String userId, String password) {
        String formData =
                String.format(
                        "szWork=login&szType=json&szUid=%s&szPassword=%s",
                        URLEncoder.encode(userId, StandardCharsets.UTF_8),
                        URLEncoder.encode(password, StandardCharsets.UTF_8));

        return httpClient
                .postForm(LOGIN_URL, formData)
                .thenApply(
                        response -> {
                            if (response.statusCode() != 200) {
                                throw new SOOPChatException(
                                        "로그인 요청 실패. 상태 코드: " + response.statusCode());
                            }

                            try {
                                JsonObject json =
                                        JsonParser.parseString(response.body()).getAsJsonObject();
                                int result = json.has("RESULT") ? json.get("RESULT").getAsInt() : 0;

                                Map<String, String> cookies =
                                        parseCookies(response.headers().allValues("set-cookie"));

                                if (result == 1) {
                                    return new AuthCookie(
                                            userId,
                                            true,
                                            response.body(),
                                            cookies.getOrDefault("AuthTicket", ""),
                                            cookies.getOrDefault("AbroadChk", ""),
                                            cookies.getOrDefault("AbroadVod", ""),
                                            cookies.getOrDefault("BbsTicket", ""),
                                            cookies.getOrDefault("RDB", ""),
                                            cookies.getOrDefault("UserTicket", ""),
                                            cookies.getOrDefault("_au", ""),
                                            cookies.getOrDefault("_au3rd", ""),
                                            cookies.getOrDefault("_ausa", ""),
                                            cookies.getOrDefault("_ausb", ""));
                                } else {
                                    String reason =
                                            json.has("REASON")
                                                    ? json.get("REASON").getAsString()
                                                    : "알 수 없는 오류";
                                    LOGGER.warning("로그인 실패: " + reason);
                                    return new AuthCookie(
                                            userId,
                                            false,
                                            response.body(),
                                            "",
                                            "",
                                            "",
                                            "",
                                            "",
                                            "",
                                            "",
                                            "",
                                            "",
                                            "");
                                }
                            } catch (Exception e) {
                                LOGGER.log(Level.WARNING, "로그인 응답 파싱 오류", e);
                                throw new SOOPChatException("로그인 응답 파싱 실패", e);
                            }
                        });
    }

    private Map<String, String> parseCookies(List<String> setCookieHeaders) {
        Map<String, String> cookies = new HashMap<>();
        for (String header : setCookieHeaders) {
            try {
                List<HttpCookie> parsed = HttpCookie.parse(header);
                for (HttpCookie cookie : parsed) {
                    cookies.put(cookie.getName(), cookie.getValue());
                }
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "쿠키 파싱 실패: " + header, e);
            }
        }
        return cookies;
    }
}
