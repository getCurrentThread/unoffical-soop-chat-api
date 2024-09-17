# 비공식 아프리카TV 채팅 API

이 프로젝트는 아프리카TV의 채팅 시스템과 상호 작용할 수 있는 비공식 Java 라이브러리입니다. 개발자들이 아프리카TV 채팅방에 연결하고, 메시지를 수신하며, 다양한 이벤트를 처리할 수 있도록 해줍니다.

## 주요 기능

- WebSocket을 사용하여 아프리카TV 채팅방에 연결
- 다양한 메시지 유형(채팅 메시지, 풍선, 이모티콘 등) 디코딩 및 처리
- 채팅 이벤트 처리를 위한 사용하기 쉬운 옵저버 패턴
- 연결 유지를 위한 자동 재연결 및 핑 메커니즘

## 필요 조건

- Java 17 이상
- Gradle 8.10.1 이상

## 설치

이 라이브러리는 아직 Maven Central에서 사용할 수 없습니다. 프로젝트에서 사용하려면 이 저장소를 복제하고 로컬 종속성으로 포함시킬 수 있습니다.

1. 저장소 복제:

   ```
   git clone https://github.com/getCurrentThread/unofficial-afreecatv-chat-api.git
   ```

2. 프로젝트 빌드:

   ```
   cd unofficial-afreecatv-chat-api
   ./gradlew build
   ```

3. 빌드된 JAR 파일을 프로젝트의 종속성에 포함시킵니다.

## 사용 방법

아프리카TV 채팅 API를 사용하는 기본 예제입니다:

```java
import com.github.getcurrentthread.afreecatvapi.client.AfreecaTVChatClient;
import com.github.getcurrentthread.afreecatvapi.client.IChatMessageObserver;

public class Example {
    public static void main(String[] args) throws Exception {
        AfreecaTVChatClient client = new AfreecaTVChatClient.Builder()
            .bid("방송인ID")
            .build();

        client.addObserver(new IChatMessageObserver() {
            @Override
            public void notify(Map<String, Object> message) {
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

- 채팅 메시지
- 풍선
- OGQ 이모티콘
- 매니저 채팅 메시지
- 초콜릿
- 퀵뷰
- 선물 티켓
- 애드콘 효과
- 비디오 풍선
- 구독
- 아이템 드롭
- 젬 아이템
- 실시간 자막

## 기여하기

기여는 언제나 환영합니다! Pull Request를 제출해 주세요.

이 프로젝트가 도움이 되셨다면, 별(⭐️)을 눌러주세요. 감사합니다!

## 라이선스

이 프로젝트는 Apache License 2.0 하에 라이선스가 부여됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 면책 조항

이는 비공식 API이며 아프리카TV와 제휴되거나 승인되지 않았습니다. 사용에 따른 책임은 사용자에게 있습니다.

\*warning: 아프리카TV 플랫폼의 웹소켓 통신 방식이 변경되면 동작하지 않을 수 있습니다. (일부 미구현 및 malfunction 있음)
