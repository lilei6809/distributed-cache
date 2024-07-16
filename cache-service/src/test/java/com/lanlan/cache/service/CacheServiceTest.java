package com.lanlan.cache.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Ref;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CacheServiceTest {

    private CacheService cacheService;

    @BeforeEach
    void setUp() {
        cacheService = new CacheService();
        ReflectionTestUtils.setField(cacheService, "cacheCapacity", 5);
        cacheService.init(); // Manually call init method
    }

    @Test
    void testPutAndGet() {
        cacheService.put("key1", "value1");
        Optional<String> result = cacheService.get("key1");
        assertTrue(result.isPresent());
        assertEquals("value1", result.get());
    }

    @Test
    void testGetNonExistentKey() {
        Optional<String> result = cacheService.get("nonexistent");
        assertFalse(result.isPresent());
    }

    @Test
    void testRemove() {
        cacheService.put("key2", "value2");
        cacheService.remove("key2");
        Optional<String> result = cacheService.get("key2");
        assertFalse(result.isPresent());
    }

    @Test
    void testGetSize() {
        assertEquals(0, cacheService.getSize());
        cacheService.put("key3", "value3");
        assertEquals(1, cacheService.getSize());
    }

    @Test
    void testGetEvictionCount() {
        // This test might be tricky as we need to force evictions
        // For now, we'll just check if the initial count is 0
        // Fill the cache
        for (int i = 0; i < 5; i++) {
            cacheService.put("key" + i, "value" + i);
        }

        // Access some items to make them recently used
        // 0,1,2 is the newest
        for (int i = 0; i < 3; i++) {
            cacheService.get("key" + i);
        }

        // Insert new items to force eviction
        // 5 - 9 is the newest
        for (int i = 5; i < 10; i++) {
            cacheService.put("key" + i, "value" + i);
        }

        // Check if the recently used items are still in the cache
        int recentlyUsedCount = 0;
        for (int i = 0; i < 3; i++) {
            if (cacheService.get("key" + i).isPresent()) {
                recentlyUsedCount++;
            }
        }

        // cache: [5-9] [0-2] [3-4]
        cacheService.put("key"+10, "value"+10);
        cacheService.put("key"+11, "value"+11);

        // [3-4] should be evicted
        assertTrue(recentlyUsedCount > 0, "Some recently used items should still be in the cache");
        assertTrue(cacheService.getEvictionCount() > 0
        );
    }
}