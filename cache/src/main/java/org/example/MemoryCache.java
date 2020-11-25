package org.example;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@ThreadSafe
public class MemoryCache<K, V> implements Cache<K, V> {
    private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<K, V>();

    private final Function<K, V> missValueProvider;

    public MemoryCache(Function<K, V> missValueProvider) {
        this.missValueProvider = missValueProvider;
    }

    public V get(K key) {
        // If not ConcurrentHashMap, need use synchronized() for thread safe
        // ConcurrentHashMap already implement the thread safe and only call
        // function once for missing value in computeIfAbsent
        return cache.computeIfAbsent(key, missValueProvider);
    }

    public void put(K key, V value) {
        cache.put(key, value);
    }
}
