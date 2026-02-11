package com.tenmo2003;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;

import com.tenmo2003.snowflakeid.SnowflakeIdGenerator;

/**
 * Unit test for simple App.
 */
class GeneratorTest {

    /**
     * Rigorous Test :-)
     */
    @Test
    void shouldAnswerWithTrue() {
        assertTrue(true);
    }

    @Test
    void testConcurrentIdGeneration() throws InterruptedException {
        int threadCount = 10_000;
        int idsPerThread = 2000;
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(Instant.now().toEpochMilli());

        Set<Long> generatedIds = ConcurrentHashMap.newKeySet();
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            Thread.ofVirtual().start(() -> {
                try {
                    latch.await();
                    for (int j = 0; j < idsPerThread; j++) {
                        generatedIds.add(generator.generateId());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown();
        doneLatch.await();

        assertEquals(threadCount * idsPerThread, generatedIds.size(), "Duplicate IDs found!");
    }
}
