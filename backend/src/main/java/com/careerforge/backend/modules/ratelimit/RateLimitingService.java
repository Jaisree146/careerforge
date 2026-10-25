package com.careerforge.backend.modules.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enterprise Token Bucket Rate Limiting Service.
 * Implements granular rate limiting policies:
 * 1. Authentication endpoints: Protects against brute-force login/register attacks.
 * 2. Generative AI endpoints: Protects Google Gemini LLM API quotas from rapid consumption.
 * 3. General endpoints: Protects MySQL database and backend resources from abusive traffic spikes.
 */
@Service
public class RateLimitingService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingService.class);

    private final long authCapacity;
    private final long aiCapacity;
    private final long generalCapacity;

    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> aiBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> generalBuckets = new ConcurrentHashMap<>();

    public RateLimitingService(
            @Value("${app.rate-limit.auth.capacity:10}") long authCapacity,
            @Value("${app.rate-limit.ai.capacity:5}") long aiCapacity,
            @Value("${app.rate-limit.general.capacity:100}") long generalCapacity
    ) {
        this.authCapacity = authCapacity;
        this.aiCapacity = aiCapacity;
        this.generalCapacity = generalCapacity;
        log.info("RateLimitingService initialized with limits -> Auth: {}/min, AI: {}/min, General: {}/min",
                authCapacity, aiCapacity, generalCapacity);
    }

    public RateLimitResult checkAuthLimit(String key) {
        Bucket bucket = authBuckets.computeIfAbsent(key, k -> createBucket(authCapacity, Duration.ofMinutes(1)));
        return consume(bucket, key, "AUTH");
    }

    public RateLimitResult checkAiLimit(String key) {
        Bucket bucket = aiBuckets.computeIfAbsent(key, k -> createBucket(aiCapacity, Duration.ofMinutes(1)));
        return consume(bucket, key, "AI");
    }

    public RateLimitResult checkGeneralLimit(String key) {
        Bucket bucket = generalBuckets.computeIfAbsent(key, k -> createBucket(generalCapacity, Duration.ofMinutes(1)));
        return consume(bucket, key, "GENERAL");
    }

    private RateLimitResult consume(Bucket bucket, String key, String category) {
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            return new RateLimitResult(true, probe.getRemainingTokens(), 0);
        } else {
            long retryAfterSeconds = (probe.getNanosToWaitForRefill() / 1_000_000_000L) + 1;
            log.warn("Rate limit exceeded for [{}] client: {} (Retry-After: {}s)", category, key, retryAfterSeconds);
            return new RateLimitResult(false, 0, retryAfterSeconds);
        }
    }

    private Bucket createBucket(long capacity, Duration refillDuration) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, refillDuration)
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    public record RateLimitResult(boolean allowed, long remainingTokens, long retryAfterSeconds) {}
}
