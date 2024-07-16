package com.lanlan.cache.core;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents an entry in the cache.
 * Each entry contains a key-value pair and metadata about the entry's usage.
 *
 * @param <K> the type of the key
 * @param <V> the type of the value
 */
public class CacheEntry<K, V> implements Serializable {
    // The key of this cache entry
    private final K key;

    // The value associated with the key
    private V value;

    // Timestamp of the last access to this entry, used for LRU eviction
    private final AtomicLong lastAccessTime;

    /**
     * Constructs a new cache entry with the given key and value.
     *
     * @param key the key of the entry
     * @param value the value associated with the key
     */
    public CacheEntry(K key, V value) {
        this.key = key;
        this.value = value;
        this.lastAccessTime = new AtomicLong(System.nanoTime());
    }

    /**
     * Returns the key of this entry.
     *
     * @return the key
     */
    public K getKey() {
        return key;
    }

    /**
     * Returns the value of this entry and updates the last access time.
     *
     * @return the value
     */
    public V getValue() {
        updateAccessTime();
        return value;
    }

    /**
     * Sets a new value for this entry and updates the last access time.
     *
     * @param value the new value to set
     */
    public void setValue(V value) {
        this.value = value;
        updateAccessTime();
    }

    /**
     * Returns the last access time of this entry.
     *
     * @return the last access time in nanoseconds
     */
    public long getLastAccessTime() {
        return lastAccessTime.get();
    }

    /**
     * Updates the last access time of this entry to the current time.
     * This method is thread-safe.
     */
    public void updateAccessTime() {
        lastAccessTime.set(System.nanoTime());
    }
}