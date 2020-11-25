package org.example;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.ws.Holder;
import java.util.HashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;

@ThreadSafe
public class MemoryCacheWithLock<K, V> implements Cache<K, V> {
    private final HashMap<K,ValueHolder<V>> cache = new HashMap<>();

    private final Function<K, V> missValueProvider;
    private final ValueHolder<V> NULL_VALUE = new ValueHolder<>(null);

    private final StampedLock lock = new StampedLock();

    public MemoryCacheWithLock(Function<K, V> missValueProvider) {
        this.missValueProvider = missValueProvider;
    }

    public @Nullable
    V get(@Nullable K key) {
        ValueHolder<V> valueHolder;
        long readLock = lock.readLock();
        try
        {
            valueHolder = cache.get(key);
        }
        finally {
            lock.unlockRead(readLock);
        }

        if (valueHolder == null) {
            long writeLock = lock.writeLock();
            try{
                // need double check for the multiple thread writer
                valueHolder = cache.get(key);
                if (valueHolder == null) {
                    V raw = missValueProvider.apply(key);

                    if (raw == null)
                        valueHolder = NULL_VALUE;
                    else
                        valueHolder = new ValueHolder<>(raw);

                    cache.put(key, valueHolder);
                }
            }
            finally {
                lock.unlockWrite(writeLock);
            }
        }

        return valueHolder.value;
    }
}
