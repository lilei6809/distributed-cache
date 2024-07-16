package com.lanlan.cache.service;

import com.lanlan.cache.core.CuckooHashCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Service
public class CacheService {

    private CuckooHashCache<String, String> cache;

    @Value("${cache.capacity:1000}")
    private int cacheCapacity;

    @PostConstruct
    public void init() {
        // Initialize the cache with a capacity
        this.cache = new CuckooHashCache<>(cacheCapacity);
    }

    public void put(String key, String value) {
        cache.put(key, value);
    }

    public Optional<String> get(String key) {
        return cache.get(key);
    }

    public void remove(String key) {
        cache.remove(key);
    }

    public int getSize() {
        return cache.getSize();
    }

    public int getEvictionCount() {
        return cache.getEvictionCount();
    }
}
