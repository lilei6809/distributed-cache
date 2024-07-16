package com.lanlan.cache.config;

public class CacheConfig {
    private int capacity;
    private long expirationTimeInMillis;

    public CacheConfig(int capacity, long expirationTimeInMillis) {
        this.capacity = capacity;
        this.expirationTimeInMillis = expirationTimeInMillis;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public long getExpirationTimeInMillis() {
        return expirationTimeInMillis;
    }

    public void setExpirationTimeInMillis(long expirationTimeInMillis) {
        this.expirationTimeInMillis = expirationTimeInMillis;
    }
}