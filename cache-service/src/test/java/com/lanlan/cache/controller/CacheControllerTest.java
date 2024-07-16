package com.lanlan.cache.controller;

import com.lanlan.cache.service.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CacheControllerTest {

    private CacheController cacheController;
    private CacheService cacheService;

    // 在每个测试方法执行之前，setUp 方法会被调用。
    @BeforeEach
    void setUp() {
        cacheService = mock(CacheService.class);
        cacheController = new CacheController(cacheService);
    }

    @Test
    void testPut() {
        ResponseEntity<Void> response = cacheController.put("key1", "value1");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(cacheService).put("key1", "value1");
    }

    @Test
    void testGetExistingKey() {
        when(cacheService.get("key1")).thenReturn(Optional.of("value1"));
        ResponseEntity<String> response = cacheController.get("key1");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("value1", response.getBody());
    }

    @Test
    void testGetNonExistentKey() {
        when(cacheService.get("nonexistent")).thenReturn(Optional.empty());
        ResponseEntity<String> response = cacheController.get("nonexistent");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testRemove() {
        ResponseEntity<Void> response = cacheController.remove("key1");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(cacheService).remove("key1");
    }

    @Test
    void testGetStats() {
        when(cacheService.getSize()).thenReturn(5);
        when(cacheService.getEvictionCount()).thenReturn(2);
        ResponseEntity<CacheController.CacheStats> response = cacheController.getStats();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5, response.getBody().size);
        assertEquals(2, response.getBody().evictionCount);
    }
}