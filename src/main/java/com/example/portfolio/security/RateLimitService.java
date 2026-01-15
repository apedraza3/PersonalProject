package com.example.portfolio.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting service using token bucket algorithm (Bucket4j)
 * Prevents brute force attacks on authentication endpoints
 */
@Service
public class RateLimitService {

    // Store buckets per IP address
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * Check if request is allowed based on rate limit
     * @param key Unique identifier (e.g., IP address or user email)
     * @param maxRequests Maximum number of requests allowed
     * @param duration Time window for the rate limit
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean tryConsume(String key, int maxRequests, Duration duration) {
        Bucket bucket = cache.computeIfAbsent(key, k -> createBucket(maxRequests, duration));
        return bucket.tryConsume(1);
    }

    /**
     * Check if request is allowed for authentication endpoints (stricter limit)
     * 5 attempts per 15 minutes per IP
     */
    public boolean tryConsumeAuth(String ipAddress) {
        return tryConsume("auth:" + ipAddress, 5, Duration.ofMinutes(15));
    }

    /**
     * Check if request is allowed for general API endpoints
     * 100 requests per minute per IP
     */
    public boolean tryConsumeApi(String ipAddress) {
        return tryConsume("api:" + ipAddress, 100, Duration.ofMinutes(1));
    }

    /**
     * Reset rate limit for a specific key (useful for successful login)
     */
    public void reset(String key) {
        cache.remove(key);
    }

    /**
     * Create a new bucket with specified limits
     */
    private Bucket createBucket(int maxRequests, Duration duration) {
        Bandwidth limit = Bandwidth.classic(maxRequests, Refill.intervally(maxRequests, duration));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Clean up old buckets (call this periodically to prevent memory leaks)
     * In production, consider using a distributed cache like Redis
     */
    public void cleanup() {
        // Simple cleanup: clear all buckets
        // In production, you'd want to track last access time and remove old entries
        if (cache.size() > 10000) {
            cache.clear();
        }
    }
}
