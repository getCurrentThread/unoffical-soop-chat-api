# 비공식 SOOP 채팅 API

[![CI](https://github.com/getCurrentThread/soopapi/actions/workflows/ci.yml/badge.svg)](https://github.com/getCurrentThread/soopapi/actions/workflows/ci.yml)
[![JitPack](https://jitpack.io/v/getCurrentThread/soopapi.svg)](https://jitpack.io/#getCurrentThread/soopapi)

이 프로젝트는 SOOP의 채팅 시스템과 상호 작용할 수 있는 비공식 Java 라이브러리입니다. 개발자들이 SOOP 채팅방에 연결하고, 메시지를 수신하며, 다양한 이벤트를 처리할 수 있도록 해줍니다.

## 주요 기능

- **이벤트 기반 아키텍처**: 타입 안전한 `on(event, handler)` 패턴으로 이벤트 구독
- **Sealed 이벤트 계층**: `ChatBaseEvent`, `DonationBaseEvent`, `SystemBaseEvent` 등 6개 카테고리로 분류된 이벤트 타입
- **93개 이벤트 타입 지원**: 채팅 메시지, 풍선, 이모티콘, 구독 등 모든 이벤트를 Java Record로 디코딩
- **연결 상태 이벤트**: `DISCONNECTED`, `RECONNECTING`, `RECONNECTED` 이벤트로 연결 라이프사이클 추적
- **Virtual Threads**: JDK 21+ Virtual Thread 기반 비동기 메시지 처리
- **통합 API 클라이언트**: `SOOPClient` 파사드로 인증, 방송 정보, 채널 정보, 채팅을 통합 관리
- **채팅 전송 지원**: `sendChat()` 메서드로 채팅 메시지 전송
- **익명(읽기 전용) 연결**: 인증 없이 채팅 수신 가능
- WebSocket 기반 자동 재연결 및 핑 메커니즘
- 이벤트 리스너 에러 핸들링

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
    implementation 'com.github.getCurrentThread:soopapi:v0.6.1'
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

### 통합 클라이언트 (SOOPClient)

```java
import com.github.getcurrentthread.soopapi.SOOPClient;
import com.github.getcurrentthread.soopapi.api.model.*;
import com.github.getcurrentthread.soopapi.client.SOOPChatClient;
import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.*;

public class Example {
    public static void main(String[] args) throws Exception {
        SOOPClient client = new SOOPClient();

        // 방송 정보 조회
        LiveDetail detail = client.live().detail("streamerId").join();
        System.out.println("방송 제목: " + detail.title());

        // 채널 정보 조회
        StationInfo station = client.channel().station("streamerId").join();
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

        // connectToChat().join()은 연결이 해제될 때까지 블로킹됩니다
        chat.connectToChat().join();
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

        // connectAndAwait()는 연결이 해제될 때까지 블로킹됩니다
        client.connectAndAwait();
    }
}
```

> **참고**: `SOOPChatClient` 생성자는 네트워크 호출을 하지 않습니다. BNO가 설정되지 않은 경우 `connectToChat()` 호출 시 자동으로 해석됩니다.

### 익명(읽기 전용) 연결

`authCookie` 없이 연결하면 익명 모드로 동작합니다. 채팅 메시지와 이벤트를 수신할 수 있지만, `sendChat()`을 호출하면 `AuthenticationException`이 발생합니다.

```java
// 인증 없이 읽기 전용으로 연결
SOOPChatConfig config = new SOOPChatConfig.Builder()
        .bid("streamerId")
        .build();

SOOPChatClient client = new SOOPChatClient(config);

client.on(ChatEvent.CHAT_MESSAGE, (ChatMessageEvent e) -> {
    System.out.println(e.senderNickname() + ": " + e.message());
});

client.connectAndAwait(); // 채팅 수신 가능, 전송 불가
```

### 인증 (채팅 전송 시 필수)

읽기 전용(이벤트 수신)은 인증 없이 사용할 수 있지만, `sendChat()`으로 채팅을 전송하려면 반드시 인증이 필요합니다.

```java
SOOPClient client = new SOOPClient();

// 1. 로그인 (실패 시 AuthenticationException 발생)
AuthCookie cookie = client.auth().signIn("userId", "password").join();
System.out.println("로그인 성공");

// 2. 인증된 설정으로 채팅 클라이언트 생성
SOOPChatConfig config = new SOOPChatConfig.Builder()
        .bid("streamerId")
        .authCookie(cookie)
        .build();

SOOPChatClient chat = new SOOPChatClient(config);

chat.on(ChatEvent.CHAT_MESSAGE, (ChatMessageEvent e) -> {
    System.out.println(e.senderNickname() + ": " + e.message());
});

// 3. 연결 (비동기)
chat.connectToChat();

// 4. 연결 완료 후 채팅 전송
chat.sendChat("Hello!").join();
```

### 연결 상태 이벤트

연결 라이프사이클을 추적할 수 있는 시스템 이벤트가 제공됩니다.

```java
import com.github.getcurrentthread.soopapi.event.model.*;

// 연결 해제 감지
client.on(ChatEvent.DISCONNECTED, (DisconnectedEvent e) -> {
    System.out.println("연결 해제: code=" + e.statusCode()
            + ", reason=" + e.reason()
            + ", error=" + e.causedByError());
});

// 재연결 시도 감지
client.on(ChatEvent.RECONNECTING, (ReconnectingEvent e) -> {
    System.out.println("재연결 시도 " + e.attemptNumber()
            + "/" + e.maxAttempts()
            + " (" + e.delayMs() + "ms 후)");
});

// 재연결 완료 감지
client.on(ChatEvent.RECONNECTED, (ReconnectedEvent e) -> {
    System.out.println("재연결 완료 (총 " + e.totalAttempts() + "회 시도)");
});
```

### 에러 핸들링

이벤트 리스너에서 발생하는 예외를 중앙에서 처리할 수 있습니다.

```java
import com.github.getcurrentthread.soopapi.exception.EventEmitterException;

client.getEventEmitter().setErrorHandler((EventEmitterException ex) -> {
    System.err.println("이벤트 처리 중 오류: " + ex.getChatEvent());
    ex.printStackTrace();
});
```

### 고급 설정

`SOOPChatConfig.Builder`를 통해 연결 동작을 세밀하게 제어할 수 있습니다.

```java
SOOPChatConfig config = new SOOPChatConfig.Builder()
        .bid("streamerId")
        .bno("12345")                                  // 방송 번호 (생략 시 자동 해석)
        .connectionTimeout(Duration.ofSeconds(15))     // 연결 타임아웃 (기본: 30초)
        .maxRetryAttempts(3)                           // 최대 재시도 횟수 (기본: 5)
        .pingIntervalSeconds(30)                       // 핑 전송 간격 (기본: 60초)
        .initialPacketDelayMs(500)                     // 초기 패킷 대기 시간 (기본: 1000ms)
        .authCookie(cookie)                            // 인증 쿠키 (생략 시 읽기 전용)
        .build();
```

## `connectToChat()` 동작 방식

v0.5.0부터 `connectToChat()`이 반환하는 `CompletableFuture`는 연결이 **해제**될 때 완료됩니다.

| 메서드 | 동작 |
|--------|------|
| `connectToChat()` | 연결 해제 시 완료되는 `CompletableFuture<Void>` 반환 |
| `connectToChat().join()` | 연결이 해제될 때까지 현재 스레드를 블로킹 |
| `connectAndAwait()` | `connectToChat().join()`의 편의 메서드 (블로킹) |
| `connectToChattingBlocking()` | **Deprecated** — `connectAndAwait()` 사용 권장 |

## 이벤트 타입

`ChatEvent` 열거형으로 모든 이벤트를 구독할 수 있습니다. 각 이벤트는 타입 안전한 Java Record로 디코딩됩니다.

### 지원 이벤트 목록

아래 이벤트는 실제 수집된 패킷으로 디코딩이 검증되었습니다.

| 이벤트 | 코드 | Record 타입 | 주요 필드 |
|--------|------|-------------|-----------|
| `LOGIN` | 1 | `LoginEvent` | `userId`, `userFlag` (익명 시 userId="") |
| `JOIN_CHANNEL` | 2 | `JoinChannelEvent` | `chatNo`, `bjId`, `maxSubBjCount`, `userFlag` |
| `CHAT_USER` | 4 | `ChatUserEvent` | `type`, `userList` (id/nickname/flag 목록) |
| `CHAT_MESSAGE` | 5 | `ChatMessageEvent` | `message`, `senderId`, `senderNickname`, `senderFlag`, `subscriptionMonth`, `randomNicknameColor` |
| `SET_BJ_STAT` | 7 | `SetBjStatEvent` | (공통 필드만, 방송 연결 해제 시 수신) |
| `SET_DUMB` | 8 | `SetDumbEvent` | `userId`, `userNickname`, `dumbTime`(초), `dumbCount`, `adminId`, `adminType` |
| `SET_USER_FLAG` | 12 | `SetUserFlagEvent` | `userId`, `userNickname`, `oldFlag`, `newFlag` |
| `SET_SUB_BJ` | 13 | `SetSubBjEvent` | `userId`, `nickname`, `flag`, `hide` |
| `SET_NICKNAME` | 14 | `SetNicknameEvent` | `userId`, `newNickname`, `oldNickname`, `changeType`, `flag` |
| `SEND_BALLOON` | 18 | `SendBalloonEvent` | `bjId`, `senderId`, `senderNickname`, `count`, `fanOrder`, `fileName`, `isDefault`, `ttsData` |
| `ICE_MODE` | 19 | `IceModeEvent` | `iceMode` (1=활성화) |
| `ICE_MODE_EX` | 21 | `IceModeExEvent` | `iceMode`, `freezeType`, `balloonLimitCount`, `subscriptionLimitCount` |
| `BJ_STICKER_ITEM` | 36 | `BjStickerItemEvent` | `type` |
| `BAN_WORD` | 54 | `BanWordEvent` | `replaceWord` (대체어), `banWordList` (금지어 배열) |
| `ADCON_EFFECT` | 87 | `AdconEffectEvent` | `bjId`, `senderId`, `senderNickname`, `adconCount`, `message`, `message2`, `urlImg`, `urlDefault` |
| `KICK_MSG_STATE` | 90 | `KickMsgStateEvent` | `chatNo`, `isHideKickMessage` |
| `FOLLOW_ITEM` | 91 | `FollowItemEvent` | `chatNo`, `recvId`, `sendId`, `sendNick`, `type` (신규 구독) |
| `FOLLOW_ITEM_EFFECT` | 93 | `FollowItemEffectEvent` | `bjId`, `sendId`, `sendNick`, `month` (연속 구독 개월 수) |
| `TRANSLATION_STATE` | 94 | `TranslationStateEvent` | `state` (1=번역 활성화) |
| `BJ_NOTICE` | 104 | `BjNoticeEvent` | `show` (1=표시), `message` (공지 내용) |
| `VIDEO_BALLOON` | 105 | `VideoBalloonEvent` | `bjId`, `userId`, `userNickname`, `balloonCount`, `fanOrder`, `isDefault`, `extraData` |
| `SEND_SUBSCRIPTION` | 108 | `SendSubscriptionEvent` | `senderId`, `senderNickname`, `receiverId`, `receiverNickname`, `itemType`, `itemCode`, `subscriptionType` |
| `OGQ_EMOTICON` | 109 | `OGQEmoticonEvent` | `chatNo`, `groupId`, `subId`, `version`, `userInfo`, `color` |
| `EMOTICON_TICKET` | 110 | `EmoticonTicketEvent` | `value` (채널 입장 직후 수신, 보통 1) |
| `ITEM_DROPS` | 111 | `ItemDropsEvent` | `bjId`, `dropsName`, `dropsMsg`, `dropsImgUrl` |
| `OGQ_EMOTICON_GIFT` | 118 | `GiftOGQEmoticonEvent` | `senderId`, `senderNick`, `receivedId`, `receivedNick`, `ogqTitle`, `ogqImageUrl` |
| `MISSION` | 121 | `MissionEvent` | `data` (JSON Map: type, mission_status, title, key, uuid) |
| `MISSION_SETTLE` | 125 | `MissionSettleEvent` | `data` (JSON Map: chno, fanOrder, list, uuid) |
| `CHUSER_EXTEND` | 127 | `ChuserExtendEvent` | `userStatus` (구독자 fw/afw 상태 맵) |
| `SEND_QUICK_VIEW` | 45 | `QuickViewEvent` | `senderId`, `senderNickname`, `receiverId`, `receiverNickname`, `itemType` |

### 연결 상태 이벤트

| 이벤트 | 코드 | Record 타입 | 주요 필드 |
|--------|------|-------------|-----------|
| `DISCONNECTED` | -3 | `DisconnectedEvent` | `statusCode`, `reason`, `causedByError` |
| `RECONNECTING` | -4 | `ReconnectingEvent` | `attemptNumber`, `maxAttempts`, `delayMs` |
| `RECONNECTED` | -5 | `ReconnectedEvent` | `totalAttempts` |

전체 93개 이벤트 타입은 `ChatEvent.java`를, 모든 Record 필드 상세는 `llms-full.txt`를 참조하세요.

## AI 지원 문서

이 프로젝트는 LLM/AI 도구가 코드베이스를 빠르게 이해할 수 있도록 AI-ready 문서를 제공합니다.

- [`llms.txt`](llms.txt) — 라이브러리 사용자를 위한 간결한 API 가이드
- [`llms-full.txt`](llms-full.txt) — 기여자를 위한 아키텍처·내부 구조 전체 레퍼런스

AI 코딩 도구(Cursor, Claude Code, GitHub Copilot 등)와 함께 작업할 때, 프롬프트에 아래 내용을 포함하면 더 정확한 코드를 생성할 수 있습니다:

> 이 프로젝트는 SOOP 채팅 API Java 라이브러리입니다. `llms.txt` 또는 `llms-full.txt` 파일을 참고하여 프로젝트의 구조, API, 이벤트 시스템을 이해한 후 작업해 주세요.

## 기여하기

기여는 언제나 환영합니다! Pull Request를 제출해 주세요.

이 프로젝트가 도움이 되셨다면, ⭐ 별을 눌러주세요. 감사합니다!

## 라이선스

이 프로젝트는 MIT License 하에 라이선스가 부여됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 면책 조항

이는 비공식 API이며 SOOP와 제휴되거나 승인되지 않았습니다. 사용에 따른 책임은 사용자에게 있습니다.

_주의: SOOP 플랫폼의 웹소켓 통신 방식이 변경되면 동작하지 않을 수 있습니다._
