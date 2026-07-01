package com.matchmetrics.security;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ApiRateLimiter {

    private final int maxRequests;
    private final long windowMs;
    private final ConcurrentHashMap<String, Bucket> cache = new ConcurrentHashMap<>();

    private record Bucket(AtomicInteger hits, long windowStart) {}

    public ApiRateLimiter(int maxRequests, long windowMs) {
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
    }

    public boolean allow(String key) {
        long now = System.currentTimeMillis();
        Bucket bucket = cache.compute(key, (k, b) -> {
            if (b == null || now - b.windowStart() > windowMs) {
                return new Bucket(new AtomicInteger(1), now);
            }
            b.hits().incrementAndGet();
            return b;
        });
        return bucket.hits().get() <= maxRequests;
    }
}
