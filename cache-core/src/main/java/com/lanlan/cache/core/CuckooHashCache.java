package com.lanlan.cache.core;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A thread-safe implementation of a cache using Cuckoo hashing.
 * This cache uses two hash tables and two hash functions to achieve
 * constant-time average case performance for insertions and lookups.
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
public class CuckooHashCache<K, V> {
    // Maximum number of attempts to insert an item before forcing an eviction
    private static final int MAX_LOOP = 100;

    // The capacity of each of the two hash tables
    private final int capacity;

    // The two hash tables
    private final CacheEntry<K, V>[] table1;
    private final CacheEntry<K, V>[] table2;

    // Lock for ensuring thread-safety
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    // Random number generator for choosing which table to evict from
    private final Random random = new Random();

    // Counter for the number of evictions that have occurred
    private final AtomicInteger evictionCount = new AtomicInteger(0);

    // Counter for the current number of items in the cache
    private final AtomicInteger size = new AtomicInteger(0);

    /**
     * Constructs a new CuckooHashCache with the specified capacity.
     *
     * @param capacity the capacity of each of the two hash tables
     */
    @SuppressWarnings("unchecked")
    public CuckooHashCache(int capacity) {
        this.capacity = capacity;
        this.table1 = new CacheEntry[capacity];
        this.table2 = new CacheEntry[capacity];
    }

    /**
     * First hash function.
     *
     * @param key the key to hash
     * @return the hash value
     */
    private int hash1(K key) {
        return Math.abs(key.hashCode()) % capacity;
    }

    /**
     * Second hash function.
     *
     * @param key the key to hash
     * @return the hash value
     */
    private int hash2(K key) {
        return Math.abs(key.hashCode() * 31 + 17) % capacity;
    }

    /**
     * Retrieves the value associated with the given key.
     *
     * @param key the key whose associated value is to be returned
     * @return an Optional containing the value to which the specified key is mapped,
     *         or an empty Optional if this cache contains no mapping for the key
     */
    public Optional<V> get(K key) {
        lock.readLock().lock();
        try {
            // Check the first table
            int h1 = hash1(key);
            if (table1[h1] != null && table1[h1].getKey().equals(key)) {
                table1[h1].updateAccessTime();
                return Optional.of(table1[h1].getValue());
            }
            // Check the second table
            int h2 = hash2(key);
            if (table2[h2] != null && table2[h2].getKey().equals(key)) {
                table2[h2].updateAccessTime();
                return Optional.of(table2[h2].getValue());
            }
            // Key not found
            return Optional.empty();
        } finally {
            // release lock
            lock.readLock().unlock();
        }
    }

    /**
     * Associates the specified value with the specified key in this cache.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     */
    public void put(K key, V value) {
        // write lock
        lock.writeLock().lock();
        try {
            // If the cache is full, perform an eviction
            if (size.get() >= capacity * 2) {
                evictAndInsert(key, value);
                return;
            }

            // Try to insert the new entry
            for (int i = 0; i < MAX_LOOP; i++) {
                // Try to insert into the first table
                int h1 = hash1(key);
                // if table1[h1] is available, insert into h1
                if (table1[h1] == null) {
                    table1[h1] = new CacheEntry<>(key, value);
                    // size++
                    size.incrementAndGet();
                    return;
                }
                // if table1[h1].key == keyInserted, update value
                else if (table1[h1].getKey().equals(key)) {
                    table1[h1].setValue(value);
                    return;
                }
                // else: table1[h1] is used by other key
                // try to find a space in table2

                // Try to insert into the second table
                int h2 = hash2(key);
                if (table2[h2] == null) {
                    table2[h2] = new CacheEntry<>(key, value);
                    size.incrementAndGet();
                    return;
                } else if (table2[h2].getKey().equals(key)) {
                    table2[h2].setValue(value);
                    return;
                }

                // else:
                // table1[h1], table2[h2] are occupied by other key

                // Evict from table1 and try to reinsert
                CacheEntry<K, V> temp = table1[h1];
                table1[h1] = new CacheEntry<>(key, value);
                key = temp.getKey();
                value = temp.getValue();

                // next for loop, put temp pair into other position until reaching MAX_LOOP
            }

            // If we reach here, we couldn't insert after MAX_LOOP attempts
            evictAndInsert(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Evicts an entry from the cache and inserts a new one.
     * This method implements a simple LRU (Least Recently Used) eviction policy.
     *
     * @param key key of the new entry to insert
     * @param value value of the new entry to insert
     */
    private void evictAndInsert(K key, V value) {
        // Randomly choose which table to evict from
        int tableChoice = random.nextInt(2);
        CacheEntry<K, V>[] targetTable = (tableChoice == 0) ? table1 : table2;

        // Find the least recently used entry
        int lruIndex = 0;
        long oldestAccess = Long.MAX_VALUE;

        // iterate all Entries, find the least recent used entry
        for (int i = 0; i < capacity; i++) {
            if (targetTable[i] == null) {
                lruIndex = i;
                break;
            }
            if (targetTable[i].getLastAccessTime() < oldestAccess) {
                oldestAccess = targetTable[i].getLastAccessTime();
                lruIndex = i;
            }
        }

        // Evict the chosen entry and insert the new one
        if (targetTable[lruIndex] != null) {
            evictionCount.incrementAndGet();
        } else {
            size.incrementAndGet();
        }

        // new entry replace the oldest entry
        targetTable[lruIndex] = new CacheEntry<>(key, value);
    }

    /**
     * Removes the mapping for a key from this cache if it is present.
     *
     * @param key key whose mapping is to be removed from the cache
     */
    public void remove(K key) {
        lock.writeLock().lock();
        try {
            // Check the first table
            int h1 = hash1(key);
            if (table1[h1] != null && table1[h1].getKey().equals(key)) {
                table1[h1] = null;
                // size--
                size.decrementAndGet();
                return;
            }

            // Check the second table
            int h2 = hash2(key);
            if (table2[h2] != null && table2[h2].getKey().equals(key)) {
                table2[h2] = null;
                size.decrementAndGet();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Returns the number of evictions that have occurred.
     *
     * @return the number of evictions
     */
    public int getEvictionCount() {
        return evictionCount.get();
    }

    /**
     * Returns the current number of key-value mappings in this cache.
     *
     * @return the number of key-value mappings in this cache
     */
    public int getSize() {
        return size.get();
    }
}