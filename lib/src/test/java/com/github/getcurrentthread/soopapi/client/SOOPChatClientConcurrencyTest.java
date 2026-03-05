package com.github.getcurrentthread.soopapi.client;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.github.getcurrentthread.soopapi.config.SOOPChatConfig;

class SOOPChatClientConcurrencyTest {

    @Test
    void concurrentConnectToChat_returnsSameFuture() throws Exception {
        SOOPChatConfig config =
                new SOOPChatConfig.Builder().bid("testConcurrency").bno("99999").build();

        SOOPChatClient client = new SOOPChatClient(config);

        int threads = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        Set<CompletableFuture<Void>> futures = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < threads; i++) {
            Thread.startVirtualThread(
                    () -> {
                        try {
                            startLatch.await();
                            CompletableFuture<Void> future = client.connectToChat();
                            futures.add(future);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            doneLatch.countDown();
                        }
                    });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS), "All threads should complete");

        // ReentrantLock이 disconnectFuture가 하나만 생성되도록 보장한다.
        // 첫 번째 호출이 isConnected=true를 설정(또는 disconnectFuture를 생성)한 후,
        // 이후 호출들은 동일한 future를 반환해야 한다.
        // 실제 ConnectionManager가 유효하지 않은 bid에 연결을 시도하므로 모든 호출이
        // 실패할 가능성이 높지만, 여전히 동일한 disconnectFuture를 공유해야 한다.
        assertEquals(
                1,
                futures.size(),
                "Concurrent connectToChat() should return the same disconnectFuture, got "
                        + futures.size()
                        + " distinct futures");

        client.disconnect();
    }
}
