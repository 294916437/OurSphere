package com.theoyu.oursphere.auth;

import com.theoyu.oursphere.auth.utils.generator.IdGeneratorHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class IdGeneratorServiceTest {

    @Resource
    private IdGeneratorHelper idGeneratorService;

    @Test
    void testGenerateId() {
        long id = idGeneratorService.generateId();
        log.info("id:{}", id);
    }
    @Test
    void testGenerateStringId() {
        String id = idGeneratorService.generateStringId();
        log.info("id:{}", id);
    }

    @Test
    void testConcurrentGeneration() throws InterruptedException {
        int threadCount = 100;
        int idsPerThread = 100;
        Set<Long> ids = new HashSet<>();
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < idsPerThread; j++) {
                        synchronized (ids) {
                            ids.add(idGeneratorService.generateId());
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // 验证生成的ID全部唯一
        assertEquals(threadCount * idsPerThread, ids.size());
    }
}