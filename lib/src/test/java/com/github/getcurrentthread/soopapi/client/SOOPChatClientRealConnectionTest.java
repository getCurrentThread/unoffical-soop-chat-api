package com.github.getcurrentthread.soopapi.client;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.github.getcurrentthread.soopapi.config.SOOPChatConfig;
import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Tag("integration")
class SOOPChatClientRealConnectionTest {

    static final String BROAD_LIST_URL =
            "https://static.file.sooplive.co.kr/pc/ko_KR/main_broad_list_with_adult_json.js";
    static final int TOP_N = 10;
    static final int TEST_DURATION_MINUTES = 3;
    static final int MAX_TOTAL_EVENTS = 30;
    static final String LOG_DIR = "test-logs";

    static final Logger logger = Logger.getLogger(SOOPChatClientRealConnectionTest.class.getName());
    static FileHandler normalHandler;
    static FileHandler errorHandler;

    static final ConcurrentHashMap<String, AtomicLong> eventCounters = new ConcurrentHashMap<>();
    static final AtomicLong totalEvents = new AtomicLong(0);

    static final int MAX_SAMPLES_PER_TYPE = 5;
    static final ConcurrentHashMap<ChatEvent, List<BaseEvent>> eventSamples =
            new ConcurrentHashMap<>();

    @BeforeAll
    static void setupLogging() throws IOException {
        Path logDir = Path.of(LOG_DIR);
        Files.createDirectories(logDir);

        System.setProperty(
                "java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT] [%4$-7s] %2$s: %5$s%6$s%n");

        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.ALL);
        for (var handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        normalHandler = new FileHandler(LOG_DIR + "/normal.log", false);
        normalHandler.setLevel(Level.ALL);
        normalHandler.setFormatter(new SimpleFormatter());
        rootLogger.addHandler(normalHandler);

        errorHandler = new FileHandler(LOG_DIR + "/error.log", false);
        errorHandler.setLevel(Level.WARNING);
        errorHandler.setFormatter(new SimpleFormatter());
        rootLogger.addHandler(errorHandler);
    }

    @AfterAll
    static void tearDownLogging() {
        if (normalHandler != null) normalHandler.close();
        if (errorHandler != null) errorHandler.close();
    }

    @Test
    void testTop10RealConnection() throws Exception {
        // === 1단계: 방송 목록 fetch & 상위 10개 추출 ===
        logger.info("=== Fetching broadcast list ===");

        List<Map<String, Object>> topStreamers = fetchTopStreamers();
        logger.info("=== Target streamers (" + topStreamers.size() + ") ===");
        for (int i = 0; i < topStreamers.size(); i++) {
            var s = topStreamers.get(i);
            logger.info(
                    String.format(
                            "#%d BID=%s, BNO=%s, nickname=%s, viewers=%s",
                            i + 1,
                            s.get("user_id"),
                            s.get("broad_no"),
                            s.get("user_nick"),
                            s.get("total_view_cnt")));
        }

        // === 2단계: SOOPChatClient 생성 & 이벤트 리스너 등록 ===
        List<SOOPChatClient> clients = new ArrayList<>();
        List<String> bids = new ArrayList<>();

        for (var streamer : topStreamers) {
            String bid = (String) streamer.get("user_id");
            String bno = String.valueOf(streamer.get("broad_no"));
            bids.add(bid);

            SOOPChatConfig config = new SOOPChatConfig.Builder().bid(bid).bno(bno).build();
            SOOPChatClient client = new SOOPChatClient(config);

            for (ChatEvent eventType : ChatEvent.values()) {
                client.on(
                        eventType,
                        (BaseEvent event) -> {
                            String key = bid + ":" + event.eventType().name();
                            eventCounters
                                    .computeIfAbsent(key, _ -> new AtomicLong(0))
                                    .incrementAndGet();
                            totalEvents.incrementAndGet();

                            List<BaseEvent> samples =
                                    eventSamples.computeIfAbsent(
                                            event.eventType(), _ -> new CopyOnWriteArrayList<>());
                            if (samples.size() < MAX_SAMPLES_PER_TYPE) {
                                samples.add(event);
                            }

                            String logMessage = formatEventLog(bid, event);
                            logger.info(logMessage);
                        });
            }

            clients.add(client);
        }

        // === 3단계: 동시 연결 (Virtual Thread) ===
        logger.info("=== Starting concurrent connections ===");
        AtomicInteger connectedCount = new AtomicInteger(0);

        @SuppressWarnings("unchecked")
        CompletableFuture<Void>[] futures = new CompletableFuture[clients.size()];

        for (int i = 0; i < clients.size(); i++) {
            final int idx = i;
            final String bid = bids.get(i);
            final SOOPChatClient client = clients.get(i);

            futures[i] =
                    CompletableFuture.runAsync(
                                    () -> {
                                        try {
                                            client.connectToChat().get(30, TimeUnit.SECONDS);
                                            connectedCount.incrementAndGet();
                                            logger.info("[" + bid + "] Connected");
                                        } catch (Exception e) {
                                            logger.log(
                                                    Level.WARNING,
                                                    "["
                                                            + bid
                                                            + "] Connection failed: "
                                                            + e.getMessage(),
                                                    e);
                                        }
                                    },
                                    Executors.newVirtualThreadPerTaskExecutor())
                            .orTimeout(35, TimeUnit.SECONDS)
                            .exceptionally(
                                    ex -> {
                                        logger.log(
                                                Level.WARNING,
                                                "[" + bids.get(idx) + "] Connection timeout",
                                                ex);
                                        return null;
                                    });
        }

        CompletableFuture.allOf(futures).join();
        logger.info(
                "=== Connection complete: "
                        + connectedCount.get()
                        + "/"
                        + clients.size()
                        + " succeeded ===");

        assertTrue(
                connectedCount.get() >= 5,
                "At least 5 connections required, actual: " + connectedCount.get());

        // === 4단계: 최대 3분 또는 30개 이벤트까지 대기 (10초마다 상태 확인) ===
        logger.info(
                "=== Monitoring started (max "
                        + TEST_DURATION_MINUTES
                        + " min, "
                        + MAX_TOTAL_EVENTS
                        + " events) ===");

        long deadline = System.currentTimeMillis() + TEST_DURATION_MINUTES * 60_000L;
        int logIntervalSec = 0;
        while (System.currentTimeMillis() < deadline && totalEvents.get() < MAX_TOTAL_EVENTS) {
            Thread.sleep(10_000);
            logIntervalSec += 10;

            long activeClients = clients.stream().filter(SOOPChatClient::isConnected).count();
            logger.info(
                    String.format(
                            "=== [%ds elapsed] Connected clients: %d, Total events received: %d ===",
                            logIntervalSec, activeClients, totalEvents.get()));
        }

        logger.info("Monitoring ended - Total events received: " + totalEvents.get());

        // === 5단계: 정리 & 통계 ===
        logger.info("=== Final statistics ===");
        for (String bid : bids) {
            long chatCount = getCount(bid, "CHAT_MESSAGE");
            long totalForBid =
                    eventCounters.entrySet().stream()
                            .filter(e -> e.getKey().startsWith(bid + ":"))
                            .mapToLong(e -> e.getValue().get())
                            .sum();
            logger.info(
                    String.format(
                            "[%s] Total events: %d, Chat messages: %d",
                            bid, totalForBid, chatCount));
        }
        logger.info("Total events received: " + totalEvents.get());

        // === 6단계: 파싱 검증 리포트 생성 ===
        generateParsingReport();

        // 모든 클라이언트 disconnect
        for (var client : clients) {
            try {
                client.disconnect();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Disconnect error", e);
            }
        }

        logger.info("=== Test complete ===");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchTopStreamers() throws Exception {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(BROAD_LIST_URL))
                        .header("User-Agent", "Mozilla/5.0")
                        .GET()
                        .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body();

        Gson gson = new Gson();
        Map<String, Object> json =
                gson.fromJson(body, new TypeToken<Map<String, Object>>() {}.getType());

        List<Map<String, Object>> broads = (List<Map<String, Object>>) json.get("broad");
        if (broads == null || broads.isEmpty()) {
            throw new RuntimeException("Cannot fetch broadcast list (no broad array)");
        }

        logger.info("Total broadcasts: " + broads.size());

        broads.sort(
                Comparator.comparingInt(
                                (Map<String, Object> m) -> {
                                    Object cnt = m.get("total_view_cnt");
                                    if (cnt instanceof Number n) return n.intValue();
                                    return Integer.parseInt(String.valueOf(cnt));
                                })
                        .reversed());

        return broads.subList(0, Math.min(TOP_N, broads.size()));
    }

    private static String formatEventLog(String bid, BaseEvent event) {
        return switch (event) {
            case ChatMessageEvent chat ->
                    "[" + bid + "] CHAT_MESSAGE: " + chat.senderNickname() + ": " + chat.message();
            case SendBalloonEvent balloon ->
                    "["
                            + bid
                            + "] SEND_BALLOON: "
                            + balloon.senderNickname()
                            + " → "
                            + balloon.count();
            default -> {
                String raw = event.raw();
                String summary = (raw != null && raw.length() > 100) ? raw.substring(0, 100) : raw;
                yield "[" + bid + "] " + event.eventType().name() + ": " + summary;
            }
        };
    }

    private static long getCount(String bid, String eventType) {
        AtomicLong counter = eventCounters.get(bid + ":" + eventType);
        return counter != null ? counter.get() : 0;
    }

    // === 파싱 검증 관련 ===

    record FieldResult(String fieldName, Object value, boolean passed, String reason) {}

    private static List<FieldResult> validateEvent(BaseEvent event) {
        List<FieldResult> results = new ArrayList<>();
        results.add(checkNonNull("eventType", event.eventType()));
        results.add(checkNonNullString("raw", event.raw()));
        results.add(checkPositive("timestamp", event.timestamp()));

        switch (event) {
            case ChatMessageEvent e -> {
                results.add(checkNonNullString("message", e.message()));
                results.add(checkNonNullString("senderId", e.senderId()));
                results.add(checkNonNullString("senderNickname", e.senderNickname()));
                results.add(checkNonNullString("senderFlag", e.senderFlag()));
                results.add(checkNonNullString("subscriptionMonth", e.subscriptionMonth()));
                results.add(checkAny("type", e.type()));
                results.add(checkAny("chatLang", e.chatLang()));
                results.add(checkOptionalString("randomNicknameColor", e.randomNicknameColor()));
                results.add(
                        checkOptionalString(
                                "randomNicknameColorDarkmode", e.randomNicknameColorDarkmode()));
            }
            case ChatUserEvent e -> {
                results.add(checkAny("type", e.type()));
                results.add(checkNonNullCollection("userList", e.userList()));
                if (e.userList() != null && !e.userList().isEmpty()) {
                    var first = e.userList().getFirst();
                    results.add(checkNonNullString("userList[0].id", first.id()));
                    results.add(checkNonNullString("userList[0].nickname", first.nickname()));
                    results.add(checkNonNullString("userList[0].flag", first.flag()));
                }
            }
            case ChuserExtendEvent e -> {
                results.add(checkNonNullMap("userStatus", e.userStatus()));
                if (e.userStatus() != null && !e.userStatus().isEmpty()) {
                    var firstEntry = e.userStatus().entrySet().iterator().next();
                    results.add(checkNonNullString("firstKey", firstEntry.getKey()));
                    results.add(checkNonNull("firstValue", firstEntry.getValue()));
                }
            }
            case SendBalloonEvent e -> {
                results.add(checkNonNullString("bjId", e.bjId()));
                results.add(checkNonNullString("senderId", e.senderId()));
                results.add(checkNonNullString("senderNickname", e.senderNickname()));
                results.add(checkPositive("count", e.count()));
                results.add(checkAny("fanOrder", e.fanOrder()));
                results.add(checkNonNullString("fileName", e.fileName()));
                results.add(checkOptionalString("ttsData", e.ttsData()));
            }
            case OGQEmoticonEvent e -> {
                results.add(checkNonNullString("chatNo", e.chatNo()));
                results.add(checkOptionalString("message", e.message()));
                results.add(checkNonNullString("groupId", e.groupId()));
                results.add(checkNonNullString("subId", e.subId()));
                results.add(checkNonNullString("version", e.version()));
                results.add(checkNonNullString("userInfo", e.userInfo()));
                results.add(checkOptionalString("color", e.color()));
                results.add(checkOptionalString("chatLang", e.chatLang()));
                results.add(checkOptionalString("type", e.type()));
            }
            case LoginEvent e -> {
                results.add(checkOptionalString("userId", e.userId()));
                results.add(checkNonNullString("userFlag", e.userFlag()));
            }
            case JoinChannelEvent e -> {
                results.add(checkNonNullString("chatNo", e.chatNo()));
                results.add(checkNonNullString("bjId", e.bjId()));
                results.add(checkAny("maxSubBjCount", e.maxSubBjCount()));
                results.add(checkOptionalString("familyNickname", e.familyNickname()));
                results.add(checkNonNullString("userFlag", e.userFlag()));
            }
            case SetUserFlagEvent e -> {
                results.add(checkNonNullString("oldFlag", e.oldFlag()));
                results.add(checkNonNullString("userId", e.userId()));
                results.add(checkNonNullString("userNickname", e.userNickname()));
                results.add(checkNonNullString("newFlag", e.newFlag()));
            }
            case TranslationStateEvent e -> {
                results.add(checkAny("state", e.state()));
            }
            case KickMsgStateEvent e -> {
                results.add(checkNonNullString("chatNo", e.chatNo()));
                results.add(checkAny("isHideKickMessage", e.isHideKickMessage()));
            }
            case BanWordEvent e -> {
                results.add(checkNonNullString("replaceWord", e.replaceWord()));
                results.add(checkNonNull("banWordList", e.banWordList()));
                if (e.banWordList() != null) {
                    results.add(checkPositive("banWordList.length", e.banWordList().length));
                }
            }
            case BjNoticeEvent e -> {
                results.add(checkAny("show", e.show()));
                results.add(checkNonNullString("message", e.message()));
            }
            case SetSubBjEvent e -> {
                results.add(checkNonNullString("userId", e.userId()));
                results.add(checkNonNullString("flag", e.flag()));
                results.add(checkAny("hide", e.hide()));
                results.add(checkNonNullString("nickname", e.nickname()));
            }
            case EmoticonTicketEvent e -> {
                results.add(checkAny("value", e.value()));
            }
            case NoneTypeEvent e -> {
                results.add(checkAny("value", e.value()));
            }
            case UnknownEvent e -> {
                results.add(checkAny("code", e.code()));
                results.add(checkNonNullString("originalMessage", e.originalMessage()));
            }
            default -> {
                // 기본 필드는 이미 검증 완료; 처리되지 않은 타입에 대해 WARN
            }
        }
        return results;
    }

    private static FieldResult checkNonNull(String name, Object value) {
        if (value == null) {
            return new FieldResult(name, null, false, "FAIL: value is null");
        }
        return new FieldResult(name, value, true, "OK");
    }

    private static FieldResult checkNonNullString(String name, String value) {
        if (value == null) {
            return new FieldResult(name, null, false, "FAIL: string is null");
        }
        if (value.isEmpty()) {
            return new FieldResult(name, value, false, "FAIL: string is empty");
        }
        return new FieldResult(name, value, true, "OK");
    }

    private static FieldResult checkOptionalString(String name, String value) {
        if (value == null || value.isEmpty()) {
            return new FieldResult(name, value, true, "WARN: optional field is null/empty");
        }
        return new FieldResult(name, value, true, "OK");
    }

    private static FieldResult checkPositive(String name, long value) {
        if (value <= 0) {
            return new FieldResult(
                    name, value, false, "FAIL: value is not positive (" + value + ")");
        }
        return new FieldResult(name, value, true, "OK");
    }

    private static FieldResult checkAny(String name, Object value) {
        return new FieldResult(name, value, true, "OK");
    }

    private static FieldResult checkNonNullCollection(String name, Collection<?> value) {
        if (value == null) {
            return new FieldResult(name, null, false, "FAIL: collection is null");
        }
        if (value.isEmpty()) {
            return new FieldResult(name, value, false, "FAIL: collection is empty");
        }
        return new FieldResult(name, value, true, "OK: size=" + value.size());
    }

    private static FieldResult checkNonNullMap(String name, Map<?, ?> value) {
        if (value == null) {
            return new FieldResult(name, null, false, "FAIL: map is null");
        }
        if (value.isEmpty()) {
            return new FieldResult(name, value, false, "FAIL: map is empty");
        }
        return new FieldResult(name, value, true, "OK: size=" + value.size());
    }

    private static void generateParsingReport() {
        Path reportPath = Path.of(LOG_DIR, "parsing-report.log");
        logger.info("=== Step 6: Generating parsing validation report ===");

        Map<String, int[]> summaryMap = new TreeMap<>();
        int totalOk = 0, totalWarn = 0, totalFail = 0;

        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(reportPath))) {
            pw.println("========================================");
            pw.println("  SOOP Chat Event Parsing Report");
            pw.println("========================================");
            pw.println();

            var sortedEntries =
                    eventSamples.entrySet().stream()
                            .sorted(Comparator.comparing(e -> e.getKey().name()))
                            .toList();

            for (var entry : sortedEntries) {
                ChatEvent eventType = entry.getKey();
                List<BaseEvent> samples = entry.getValue();
                int[] counts = {0, 0, 0}; // 성공, 경고, 실패

                pw.printf(
                        "--- %s (%s) - %d samples ---%n",
                        eventType.name(), eventType.getDescription(), samples.size());

                for (int i = 0; i < samples.size(); i++) {
                    BaseEvent sample = samples.get(i);
                    List<FieldResult> results = validateEvent(sample);

                    pw.printf("  Sample #%d [%s]:%n", i + 1, sample.getClass().getSimpleName());
                    for (FieldResult fr : results) {
                        String status;
                        if (!fr.passed()) {
                            status = "FAIL";
                            counts[2]++;
                        } else if (fr.reason().startsWith("WARN")) {
                            status = "WARN";
                            counts[1]++;
                        } else {
                            status = "OK";
                            counts[0]++;
                        }

                        String displayValue = formatValue(fr.value());
                        pw.printf("    %-30s = %-35s [%s]%n", fr.fieldName(), displayValue, status);
                    }
                    pw.println();
                }

                summaryMap.put(eventType.name(), counts);
                totalOk += counts[0];
                totalWarn += counts[1];
                totalFail += counts[2];
            }

            pw.println("========================================");
            pw.println("  SUMMARY");
            pw.println("========================================");
            pw.printf("%-30s %5s %6s %6s%n", "EVENT TYPE", "OK", "WARN", "FAIL");
            pw.println("-".repeat(49));

            for (var entry : summaryMap.entrySet()) {
                int[] c = entry.getValue();
                pw.printf("%-30s %5d %6d %6d%n", entry.getKey(), c[0], c[1], c[2]);
            }

            pw.println("-".repeat(49));
            pw.printf("%-30s %5d %6d %6d%n", "TOTAL", totalOk, totalWarn, totalFail);
            pw.println();

            logger.info("Parsing report generated: " + reportPath.toAbsolutePath());
            logger.info(
                    String.format(
                            "SUMMARY - OK: %d, WARN: %d, FAIL: %d", totalOk, totalWarn, totalFail));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to generate parsing report", e);
        }
    }

    private static String formatValue(Object value) {
        if (value == null) return "null";
        String str = value.toString();
        if (str.length() > 35) {
            return str.substring(0, 32) + "...";
        }
        return str;
    }
}
