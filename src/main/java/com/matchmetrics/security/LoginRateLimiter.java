package com.matchmetrics.security;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 15 * 60_000L;

    private record Bucket(AtomicInteger hits, long windowStart) {}

    private final ConcurrentHashMap<String, Bucket> cache = new ConcurrentHashMap<>();

    public void checkAndRecord(String key) {
        long now = System.currentTimeMillis();
        Bucket bucket = cache.compute(key, (k, b) -> {
            if (b == null || now - b.windowStart() > WINDOW_MS) {
                return new Bucket(new AtomicInteger(1), now);
            }
            b.hits().incrementAndGet();
            return b;
        });
        if (bucket.hits().get() > MAX_ATTEMPTS) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Demasiados intentos de inicio de sesión. Espera 15 minutos e inténtalo de nuevo.");
        }
    }

    public void reset(String key) {
        cache.remove(key);
    }
}
