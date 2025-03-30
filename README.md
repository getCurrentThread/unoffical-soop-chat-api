# 비공식 SOOP 채팅 API

이 프로젝트는 SOOP의 채팅 시스템과 상호 작용할 수 있는 비공식 Java 라이브러리입니다. 개발자들이 SOOP 채팅방에 연결하고, 메시지를 수신하며, 다양한 이벤트를 처리할 수 있도록 해줍니다.

## 주요 기능

- WebSocket을 사용하여 SOOP 채팅방에 연결
- 다양한 메시지 유형(채팅 메시지, 풍선, 이모티콘 등) 디코딩 및 처리
- 채팅 이벤트 처리를 위한 사용하기 쉬운 옵저버 패턴
- 연결 유지를 위한 자동 재연결 및 핑 메커니즘
- **JDK 21 가상 스레드(Virtual Thread)를 활용한 높은 동시성 및 성능 최적화**

## 필요 조건

- **Java 21 이상**
- Gradle 8.10.1 이상

## 설치

이 라이브러리는 아직 Maven Central에서 사용할 수 없습니다. 프로젝트에서 사용하려면 이 저장소를 복제하고 로컬 종속성으로 포함시킬 수 있습니다.

1. 저장소 복제:

   ```
   git clone https://github.com/getCurrentThread/unofficial-soop-chat-api.git
   ```

2. 프로젝트 빌드:

   ```
   cd unofficial-soop-chat-api
   ./gradlew build
   ```

3. 빌드된 JAR 파일을 프로젝트의 종속성에 포함시킵니다.

## 사용 방법

SOOP 채팅 API를 사용하는 기본 예제입니다:

```java
import com.github.getcurrentthread.soopapi.client.SOOPChatClient;
import com.github.getcurrentthread.soopapi.client.IChatMessageObserver;
import com.github.getcurrentthread.soopapi.config.SOOPChatConfig;
import com.github.getcurrentthread.soopapi.model.Message;

public class Example {
   public static void main(String[] args) throws Exception {
      SOOPChatConfig config = new SOOPChatConfig.Builder()
              .bid("방송인ID")
              .build();

      SOOPChatClient client = new SOOPChatClient(config);

      client.addObserver(new IChatMessageObserver() {
         @Override
         public void notify(Message message) {
            System.out.println("수신된 메시지: " + message);
         }
      });

      client.connectToChat().join();

      // 프로그램 실행 유지
      Thread.sleep(Long.MAX_VALUE);
   }
}
```

## 메시지 유형

이 라이브러리는 다음을 포함한 다양한 메시지 유형의 디코딩을 지원합니다:

- 채팅 메시지 (CHAT_MESSAGE)
- 풍선 (SEND_BALLOON, SEND_BALLOON_SUB)
- OGQ 이모티콘 (OGQ_EMOTICON, OGQ_EMOTICON_GIFT)
- 매니저 채팅 메시지 (MANAGER_CHAT)
- 초콜릿 (CHOCOLATE, CHOCOLATE_SUB)
- 퀵뷰 (SEND_QUICK_VIEW)
- 선물 티켓 (GIFT_TICKET)
- 애드콘 효과 (ADCON_EFFECT)
- 비디오 풍선 (VIDEO_BALLOON)
- 구독 (SEND_SUBSCRIPTION)
- 아이템 드롭 (ITEM_DROPS)
- 젬 아이템 (GEM_ITEM_SEND)
- 실시간 자막 (LIVE_CAPTION)
- 채널 입장/퇴장 (JOIN_CHANNEL, QUIT_CHANNEL)
- 유저 플래그 설정 (SET_USER_FLAG)
- 매니저 설정 (SET_SUB_BJ)
- 닉네임 변경 (SET_NICKNAME)
- 얼음방 모드 (ICE_MODE, ICE_MODE_EX)
- 팬레터 (SEND_FAN_LETTER, SEND_FAN_LETTER_SUB)
- 미션 관련 (MISSION, MISSION_SETTLE)
- 번역 관련 (TRANSLATION, TRANSLATION_STATE)
- 채팅 금지 (SET_DUMB)
- 그 외 다양한 시스템 메시지

## 기여하기

기여는 언제나 환영합니다! Pull Request를 제출해 주세요.

이 프로젝트가 도움이 되셨다면, 별(⭐️)을 눌러주세요. 감사합니다!

## 라이선스

이 프로젝트는 Apache License 2.0 하에 라이선스가 부여됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 면책 조항

이는 비공식 API이며 SOOP와 제휴되거나 승인되지 않았습니다. 사용에 따른 책임은 사용자에게 있습니다.

_주의: SOOP 플랫폼의 웹소켓 통신 방식이 변경되면 동작하지 않을 수 있습니다._
