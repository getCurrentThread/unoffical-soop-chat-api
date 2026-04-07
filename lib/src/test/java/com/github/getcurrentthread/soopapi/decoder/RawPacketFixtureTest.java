package com.github.getcurrentthread.soopapi.decoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.getcurrentthread.soopapi.constant.SOOPConstants;
import com.github.getcurrentthread.soopapi.decoder.factory.DefaultMessageDecoderFactory;
import com.github.getcurrentthread.soopapi.decoder.message.IMessageDecoder;
import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.EventEmitter;
import com.github.getcurrentthread.soopapi.event.EventListener;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.util.SOOPChatUtils;

/**
 * 수집된 raw 패킷 픽스처(.bin 파일)를 사용해 각 메시지 타입이 올바르게 처리되는지 검증합니다.
 *
 * <p>픽스처 파일이 없는 경우 테스트는 자동으로 스킵됩니다. DataCollector를 {@code --fixtures} 모드로 실행하면 {@code
 * lib/src/test/resources/fixtures/} 아래에 타입별 .bin 파일이 생성됩니다.
 *
 * <p>파일 포맷: [4바이트 big-endian 길이][UTF-8 패킷 바이트] 반복
 */
class RawPacketFixtureTest {

    @ParameterizedTest(name = "[{0}] raw 패킷이 올바르게 처리됨")
    @MethodSource("fixturePackets")
    @SuppressWarnings("unchecked")
    void rawPacket_dispatchedCorrectly(ChatEvent expectedType, String rawPacket) throws Exception {
        assumeTrue(expectedType != null, "픽스처 없음 — DataCollector --fixtures 를 먼저 실행하세요");

        // 패킷 헤더에서 서비스 코드 검증
        int firstSep = rawPacket.indexOf(SOOPConstants.F_CHAR);
        assertTrue(firstSep > 0, "패킷에 구분자가 없음: " + rawPacket);
        String header = rawPacket.substring(0, firstSep);
        int serviceCode = SOOPChatUtils.parseServiceCode(header);
        assertEquals(
                expectedType.getCode(),
                serviceCode,
                String.format(
                        "패킷 서비스 코드(%d)가 예상 타입(%s, %d)과 일치하지 않음",
                        serviceCode, expectedType.name(), expectedType.getCode()));

        EventEmitter emitter = new EventEmitter();
        Map<ChatEvent, IMessageDecoder> decoders =
                new DefaultMessageDecoderFactory().createDecoders();

        // 동기 실행을 위한 익명 ExecutorService (테스트용)
        ExecutorService directExecutor =
                new java.util.concurrent.AbstractExecutorService() {
                    @Override
                    public void shutdown() {}

                    @Override
                    public List<Runnable> shutdownNow() {
                        return List.of();
                    }

                    @Override
                    public boolean isShutdown() {
                        return false;
                    }

                    @Override
                    public boolean isTerminated() {
                        return false;
                    }

                    @Override
                    public boolean awaitTermination(long timeout, TimeUnit unit) {
                        return true;
                    }

                    @Override
                    public void execute(Runnable command) {
                        command.run();
                    }
                };

        MessageDispatcher dispatcher = new MessageDispatcher(decoders, directExecutor, emitter);

        AtomicReference<BaseEvent> received = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();

        emitter.on(expectedType, (EventListener<BaseEvent>) received::set);

        try {
            dispatcher.dispatchMessage(rawPacket);
        } catch (Throwable t) {
            error.set(t);
        }

        assertNull(error.get(), "디스패치 중 예외 발생: " + error.get());
        assertNotNull(received.get(), "이벤트가 발행되지 않음 (디코더 반환값 null 의심): " + expectedType);
        assertEquals(expectedType, received.get().eventType(), "이벤트 타입 불일치");
        assertNotNull(received.get().raw(), "raw 필드가 null");
        assertTrue(received.get().timestamp() > 0, "timestamp 가 0 이하");

        // 모든 필드에 대해 null 체크 (필수값 누락 확인)
        checkFieldsNotNull(received.get());
    }

    private void checkFieldsNotNull(Object event) throws Exception {
        Class<?> clazz = event.getClass();
        // Record 인 경우 컴포넌트들을, 일반 클래스인 경우 필드들을 확인
        if (clazz.isRecord()) {
            for (java.lang.reflect.RecordComponent component : clazz.getRecordComponents()) {
                Object value = component.getAccessor().invoke(event);
                assertNotNull(
                        value,
                        String.format(
                                "이벤트(%s)의 필드(%s)가 null 입니다.",
                                clazz.getSimpleName(), component.getName()));
            }
        } else {
            // 상위 클래스의 필드들도 포함하여 확인 (필요시)
            while (clazz != null && clazz != Object.class) {
                for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                    if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    field.setAccessible(true);
                    Object value = field.get(event);
                    assertNotNull(
                            value,
                            String.format(
                                    "이벤트(%s)의 필드(%s)가 null 입니다.",
                                    event.getClass().getSimpleName(), field.getName()));
                }
                clazz = clazz.getSuperclass();
            }
        }
    }

    static Stream<Arguments> fixturePackets() {
        URL fixturesUrl = RawPacketFixtureTest.class.getClassLoader().getResource("fixtures");
        if (fixturesUrl == null) {
            // 픽스처 디렉터리 없음 → 더미 케이스로 스킵 처리
            return Stream.of(Arguments.of(null, null));
        }

        Path fixturesDir;
        try {
            fixturesDir = Path.of(fixturesUrl.toURI());
        } catch (Exception e) {
            return Stream.of(Arguments.of(null, null));
        }

        if (!Files.isDirectory(fixturesDir)) {
            return Stream.of(Arguments.of(null, null));
        }

        List<Arguments> args = new ArrayList<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(fixturesDir, "*.bin")) {
            for (Path binFile : ds) {
                String filename = binFile.getFileName().toString();
                String eventName = filename.substring(0, filename.length() - 4); // .bin 제거

                ChatEvent eventType;
                try {
                    eventType = ChatEvent.valueOf(eventName);
                } catch (IllegalArgumentException e) {
                    System.err.println("알 수 없는 픽스처 파일명, 스킵: " + filename);
                    continue;
                }

                for (String packet : readPackets(binFile)) {
                    args.add(Arguments.of(eventType, packet));
                }
            }
        } catch (IOException e) {
            System.err.println("픽스처 디렉터리 읽기 실패: " + e.getMessage());
        }

        return args.isEmpty() ? Stream.of(Arguments.of(null, null)) : args.stream();
    }

    private static List<String> readPackets(Path binFile) throws IOException {
        List<String> packets = new ArrayList<>();
        try (DataInputStream dis =
                new DataInputStream(new BufferedInputStream(Files.newInputStream(binFile)))) {
            while (dis.available() > 0) {
                int length = dis.readInt();
                byte[] bytes = new byte[length];
                dis.readFully(bytes);
                packets.add(new String(bytes, StandardCharsets.UTF_8));
            }
        }
        return packets;
    }
}
