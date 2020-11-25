package org.example;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;

@ThreadSafe
public class MemoryCache<K, V> implements Cache<K, V> {
    // in order to have the fast thread safe get() access & average O(log(n))
    private final ConcurrentSkipListMap<ValueHolder<K>, ValueHolder<V>> cache = new ConcurrentSkipListMap<>(this::compareKey);

    private final Function<K, V> missValueProvider;
    private final ValueHolder<V> NULL_VALUE = new ValueHolder<>(null);
    private final ValueHolder<K> NULL_KEY = new ValueHolder<>(null);


    public MemoryCache(Function<K, V> missValueProvider) {
        this.missValueProvider = missValueProvider;
    }

    // borrow the HashMap hash code function
    private static int hash(Object key) {
        int h;
        return (h = key.hashCode()) ^ (h >>> 16);
    }

    private int compareKey(ValueHolder<K> k, ValueHolder<K> k1) {
        int result = 0;
        K firstValue = k.value;
        K secondValue = k1.value;

        if (firstValue instanceof Comparable && secondValue instanceof Comparable) {
            result = ((Comparable) firstValue).compareTo(secondValue);
        } else if (firstValue != null && secondValue != null) {
            result = Integer.compare(hash(firstValue), hash(secondValue));
        } else {
            if (firstValue != null)
                result = 1;
            else if (secondValue != null)
                result = -1;
        }

        return result;
    }

    public @Nullable
    V get(@Nullable K key) {
        ValueHolder<K> keyHolder = key == null ? NULL_KEY : new ValueHolder<>(key);
        ValueHolder<V> valueHolder = cache.get(keyHolder);
        if (valueHolder == null) {
            synchronized (cache) {
                // need double check for the multiple thread writer
                valueHolder = cache.get(keyHolder);
                if (valueHolder == null) {
                    V raw = missValueProvider.apply(key);

                    if (raw == null)
                        valueHolder = NULL_VALUE;
                    else
                        valueHolder = new ValueHolder<>(raw);

                    cache.put(keyHolder, valueHolder);
                }
            }
        }

        return valueHolder.value;
    }
}
