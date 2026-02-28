# 비공식 SOOP 채팅 API

[![CI](https://github.com/getCurrentThread/soopapi/actions/workflows/ci.yml/badge.svg)](https://github.com/getCurrentThread/soopapi/actions/workflows/ci.yml)
[![JitPack](https://jitpack.io/v/getCurrentThread/soopapi.svg)](https://jitpack.io/#getCurrentThread/soopapi)

이 프로젝트는 SOOP의 채팅 시스템과 상호 작용할 수 있는 비공식 Java 라이브러리입니다. 개발자들이 SOOP 채팅방에 연결하고, 메시지를 수신하며, 다양한 이벤트를 처리할 수 있도록 해줍니다.

## 주요 기능

- **이벤트 기반 아키텍처**: 타입 안전한 `on(event, handler)` 패턴으로 이벤트 구독
- **Sealed 이벤트 계층**: `ChatBaseEvent`, `DonationBaseEvent`, `SystemBaseEvent` 등 6개 카테고리로 분류된 이벤트 타입
- **93개 이벤트 타입 지원**: 채팅 메시지, 풍선, 이모티콘, 구독 등 모든 이벤트를 Java Record로 디코딩
- **Virtual Threads**: JDK 21+ Virtual Thread 기반 비동기 메시지 처리
- **통합 API 클라이언트**: `SoopClient` 파사드로 인증, 방송 정보, 채널 정보, 채팅을 통합 관리
- **채팅 전송 지원**: `sendChat()` 메서드로 채팅 메시지 전송
- WebSocket 기반 자동 재연결 및 핑 메커니즘

## 필요 조건

- Java 25 이상
- Gradle 9.3.1 이상
- `--enable-preview` 플래그 필요 (Preview 기능 사용: Stable Values, Structured Concurrency)

> **참고**: 이 라이브러리는 JDK 25 Preview 기능을 사용합니다. 소비자 프로젝트에서도 컴파일 및 실행 시 `--enable-preview` 플래그를 추가해야 합니다.
>
> ```gradle
> tasks.withType(JavaCompile) {
>     options.compilerArgs.addAll(['--enable-preview'])
> }
> test {
>     jvmArgs(['--enable-preview'])
> }
> ```

## 설치

### Gradle (JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.getCurrentThread:soopapi:v0.2.0'
}
```

### 소스에서 빌드

1. 저장소 복제:

   ```
   git clone https://github.com/getCurrentThread/soopapi.git
   ```

2. 프로젝트 빌드:

   ```
   cd soopapi
   ./gradlew build
   ```

3. 빌드된 JAR 파일을 프로젝트의 종속성에 포함시킵니다.

## 사용 방법

### 통합 클라이언트 (SoopClient)

```java
import com.github.getcurrentthread.soopapi.SoopClient;
import com.github.getcurrentthread.soopapi.api.model.*;
import com.github.getcurrentthread.soopapi.client.SOOPChatClient;
import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.*;

public class Example {
    public static void main(String[] args) throws Exception {
        SoopClient client = new SoopClient();

        // 방송 정보 조회
        LiveDetail detail = client.live.detail("streamerId").join();
        System.out.println("방송 제목: " + detail.title());

        // 채널 정보 조회
        StationInfo station = client.channel.station("streamerId").join();
        System.out.println("스테이션: " + station.stationName());

        // 채팅 연결 (이벤트 기반)
        SOOPChatClient chat = client.chat("streamerId");

        chat.on(ChatEvent.CHAT_MESSAGE, (ChatMessageEvent e) -> {
            System.out.println(e.senderNickname() + ": " + e.message());
        });

        chat.on(ChatEvent.SEND_BALLOON, (SendBalloonEvent e) -> {
            System.out.println(e.senderNickname() + "님이 풍선 " + e.count() + "개 선물!");
        });

        chat.on(ChatEvent.SEND_SUBSCRIPTION, (SendSubscriptionEvent e) -> {
            System.out.println("구독 이벤트: " + e);
        });

        chat.connectToChat().join();

        // 채팅 전송
        chat.sendChat("Hello!");

        // 프로그램 실행 유지
        Thread.sleep(Long.MAX_VALUE);
    }
}
```

### 직접 연결

```java
import com.github.getcurrentthread.soopapi.client.SOOPChatClient;
import com.github.getcurrentthread.soopapi.config.SOOPChatConfig;
import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.ChatMessageEvent;

public class DirectExample {
    public static void main(String[] args) throws Exception {
        SOOPChatConfig config = new SOOPChatConfig.Builder()
                .bid("streamerId")
                .build();

        SOOPChatClient client = new SOOPChatClient(config);

        client.on(ChatEvent.CHAT_MESSAGE, (ChatMessageEvent e) -> {
            System.out.println(e.senderNickname() + ": " + e.message());
        });

        client.connectToChat().join();

        Thread.sleep(Long.MAX_VALUE);
    }
}
```

### 인증 (선택사항)

```java
SoopClient client = new SoopClient();

AuthCookie cookie = client.auth.signIn("userId", "password").join();
if (cookie.success()) {
    System.out.println("로그인 성공");
}
```

## 이벤트 타입

`ChatEvent` 열거형으로 모든 이벤트를 구독할 수 있습니다. 각 이벤트는 타입 안전한 Java Record로 디코딩됩니다.

| 이벤트 | 코드 | Record 타입 |
|--------|------|-------------|
| `CHAT_MESSAGE` | 5 | `ChatMessageEvent` |
| `SEND_BALLOON` | 18 | `SendBalloonEvent` |
| `OGQ_EMOTICON` | 109 | `OGQEmoticonEvent` |
| `SEND_SUBSCRIPTION` | 108 | `SendSubscriptionEvent` |
| `JOIN_CHANNEL` | 2 | `JoinChannelEvent` |
| `QUIT_CHANNEL` | 3 | `QuitChannelEvent` |
| `KICK` | 11 | `KickEvent` |
| `NOTICE` | 10 | `NoticeEvent` |
| `CHOCOLATE` | 37 | `ChocolateEvent` |
| `VIDEO_BALLOON` | 105 | `VideoBalloonEvent` |
| `LIVE_CAPTION` | 122 | `LiveCaptionEvent` |
| `MISSION` | 121 | `MissionEvent` |

...그 외 83개 이벤트 타입 지원. 전체 목록은 `ChatEvent.java`를 참조하세요.

## 기여하기

기여는 언제나 환영합니다! Pull Request를 제출해 주세요.

이 프로젝트가 도움이 되셨다면, 별을 눌러주세요. 감사합니다!

## 라이선스

이 프로젝트는 MIT License 하에 라이선스가 부여됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 면책 조항

이는 비공식 API이며 SOOP와 제휴되거나 승인되지 않았습니다. 사용에 따른 책임은 사용자에게 있습니다.

_주의: SOOP 플랫폼의 웹소켓 통신 방식이 변경되면 동작하지 않을 수 있습니다._
