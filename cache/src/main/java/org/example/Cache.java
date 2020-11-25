package org.example;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public interface Cache<K,V> {
    V get(K key);
}
