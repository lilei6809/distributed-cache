package com.lanlan.cache.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.*;

class CuckooHashCacheTest {

    private CuckooHashCache<String, String> cache;

    @BeforeEach
    void setUp() {
        cache = new CuckooHashCache<>(5); // Smaller capacity to easily trigger evictions
    }

    @Test
    void testPutAndGet() {
        cache.put("key1", "value1");
        assertEquals(Optional.of("value1"), cache.get("key1"));
    }

    @Test
    void testGetNonExistentKey() {
        assertEquals(Optional.empty(), cache.get("nonexistent"));
    }

    @Test
    void testPutOverwrite() {
        cache.put("key1", "value1");
        cache.put("key1", "value2");
        assertEquals(Optional.of("value2"), cache.get("key1"));
    }

    @Test
    void testRemove() {
        cache.put("key1", "value1");
        cache.remove("key1");
        assertEquals(Optional.empty(), cache.get("key1"));
    }

    @Test
    void testEviction() {
        // Fill the cache and then some
        for (int i = 0; i < 15; i++) {
            cache.put("key" + i, "value" + i);
        }

        assertTrue(cache.getEvictionCount() > 0, "Some evictions should have occurred");
        assertTrue(cache.getSize() <= 10, "Cache size should not exceed double the capacity");
    }

    @Test
    void testLRUEviction() {
        // Fill the cache
        for (int i = 0; i < 5; i++) {
            cache.put("key" + i, "value" + i);
        }

        // Access some items to make them recently used
        // 0,1,2 is the newest
        for (int i = 0; i < 3; i++) {
            cache.get("key" + i);
        }

        // Insert new items to force eviction
        // 5 - 9 is the newest
        for (int i = 5; i < 10; i++) {
            cache.put("key" + i, "value" + i);
        }

        // Check if the recently used items are still in the cache
        int recentlyUsedCount = 0;
        for (int i = 0; i < 3; i++) {
            if (cache.get("key" + i).isPresent()) {
                recentlyUsedCount++;
            }
        }

        // cache: [5-9] [0-2] [3-4]
        cache.put("key"+10, "value"+10);
        cache.put("key"+11, "value"+11);

        // [3-4] should be evicted
        assertTrue(recentlyUsedCount > 0, "Some recently used items should still be in the cache");
        assertTrue(cache.getEvictionCount() > 0, "Some evictions should have occurred");
    }


    @Test
    void testConcurrentAccess() throws InterruptedException {
        int threadCount = 10;
        int operationsPerThread = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        cache.put("key" + threadId + "-" + j, "value" + threadId + "-" + j);
                        cache.get("key" + threadId + "-" + j);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        assertTrue(cache.getEvictionCount() > 0, "Some evictions should have occurred during concurrent access");
        assertTrue(cache.getSize() <= 10, "Cache size should not exceed double the capacity");
    }
}