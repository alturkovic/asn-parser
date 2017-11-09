package com.github.alturkovic.asn.ber.collection;

import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@AllArgsConstructor
public class MultiSet<T> {
    private final Map<T, AtomicInteger> counter;

    public MultiSet() {
        this(new HashMap<>());
    }

    public int count(final T key) {
        final AtomicInteger count = counter.get(key);
        return count == null ? 0 : count.get();
    }

    public void add(final T key) {
        counter.computeIfAbsent(key, aKey -> new AtomicInteger()).incrementAndGet();
    }
}
