package com.careerforge.backend.modules.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Rate Limiting Service Unit Tests (Bucket4j Token Bucket)")
class RateLimitingServiceTest {

    private RateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        // Initialize with standard capacities: Auth=10, AI=5, General=100
        rateLimitingService = new RateLimitingService(10, 5, 100);
    }

    @Test
    @DisplayName("Should allow AI requests up to capacity (5) and reject subsequent requests with retry-after")
    void shouldEnforceAiRateLimit() {
        String clientKey = "user:candidate@example.com";

        // First 5 requests must be allowed
        for (int i = 0; i < 5; i++) {
            RateLimitingService.RateLimitResult result = rateLimitingService.checkAiLimit(clientKey);
            assertThat(result.allowed()).isTrue();
            assertThat(result.remainingTokens()).isEqualTo(4 - i);
            assertThat(result.retryAfterSeconds()).isEqualTo(0);
        }

        // 6th request must be rejected
        RateLimitingService.RateLimitResult blocked = rateLimitingService.checkAiLimit(clientKey);
        assertThat(blocked.allowed()).isFalse();
        assertThat(blocked.remainingTokens()).isEqualTo(0);
        assertThat(blocked.retryAfterSeconds()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should enforce Auth rate limit up to capacity (10) per IP")
    void shouldEnforceAuthRateLimit() {
        String ipKey = "ip:192.168.1.50";

        // Consume 10 auth tokens
        for (int i = 0; i < 10; i++) {
            RateLimitingService.RateLimitResult result = rateLimitingService.checkAuthLimit(ipKey);
            assertThat(result.allowed()).isTrue();
            assertThat(result.remainingTokens()).isEqualTo(9 - i);
        }

        // 11th request must be blocked
        RateLimitingService.RateLimitResult blocked = rateLimitingService.checkAuthLimit(ipKey);
        assertThat(blocked.allowed()).isFalse();
        assertThat(blocked.retryAfterSeconds()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should maintain independent buckets for different client identifiers")
    void shouldIsolateLimitsBetweenClients() {
        String clientA = "user:clientA@example.com";
        String clientB = "user:clientB@example.com";

        // Exhaust client A's quota
        for (int i = 0; i < 5; i++) {
            rateLimitingService.checkAiLimit(clientA);
        }
        assertThat(rateLimitingService.checkAiLimit(clientA).allowed()).isFalse();

        // Client B must still have full quota
        RateLimitingService.RateLimitResult bResult = rateLimitingService.checkAiLimit(clientB);
        assertThat(bResult.allowed()).isTrue();
        assertThat(bResult.remainingTokens()).isEqualTo(4);
    }

    @Test
    @DisplayName("Should maintain independent buckets across different endpoint categories")
    void shouldIsolateLimitsAcrossCategories() {
        String client = "user:test@example.com";

        // Exhaust AI limit (5 tokens)
        for (int i = 0; i < 5; i++) {
            rateLimitingService.checkAiLimit(client);
        }
        assertThat(rateLimitingService.checkAiLimit(client).allowed()).isFalse();

        // General limit for same client must still be completely available
        RateLimitingService.RateLimitResult generalResult = rateLimitingService.checkGeneralLimit(client);
        assertThat(generalResult.allowed()).isTrue();
        assertThat(generalResult.remainingTokens()).isEqualTo(99);
    }
}
