package com.careerforge.backend.modules.ratelimit;

import com.careerforge.backend.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Production-Grade Rate Limiting Filter.
 * Executes after JwtAuthenticationFilter to enable authenticated per-user rate limiting
 * with seamless fallback to client IP address for unauthenticated requests.
 *
 * Implements standard rate limiting response headers:
 * - X-Rate-Limit-Remaining: Tokens remaining in bucket for the current window.
 * - X-Rate-Limit-Retry-After-Seconds: Number of seconds to wait before retrying (on 429).
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final RateLimitingService rateLimitingService;
    private final ObjectMapper objectMapper;

    public RateLimitingFilter(RateLimitingService rateLimitingService, ObjectMapper objectMapper) {
        this.rateLimitingService = rateLimitingService;
        this.objectMapper = objectMapper.copy().findAndRegisterModules();
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Allow CORS preflight requests to pass through untouched
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String uri = request.getRequestURI();

        // Only enforce rate limits on API routes
        if (!uri.startsWith("/api/v1/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = resolveClientKey(request);
        RateLimitingService.RateLimitResult result;

        if (uri.startsWith("/api/v1/auth/")) {
            result = rateLimitingService.checkAuthLimit(clientKey);
        } else if (uri.startsWith("/api/v1/ai/")) {
            result = rateLimitingService.checkAiLimit(clientKey);
        } else {
            result = rateLimitingService.checkGeneralLimit(clientKey);
        }

        if (!result.allowed()) {
            log.warn("Rate limit exceeded for clientKey: {}, URI: {}", clientKey, uri);
            writeRateLimitExceededResponse(response, result.retryAfterSeconds());
            return;
        }

        response.setHeader("X-Rate-Limit-Remaining", String.valueOf(result.remainingTokens()));
        filterChain.doFilter(request, response);
    }

    private void writeRateLimitExceededResponse(HttpServletResponse response, long retryAfterSeconds) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(retryAfterSeconds));

        ApiResponse<Void> errorResponse = ApiResponse.failure(
                "Rate limit exceeded. Please retry after " + retryAfterSeconds + " seconds."
        );
        try {
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        } catch (Exception e) {
            response.getWriter().write(String.format(
                    "{\"success\":false,\"message\":\"Rate limit exceeded. Please retry after %d seconds.\",\"data\":null}",
                    retryAfterSeconds
            ));
        }
    }

    private String resolveClientKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "user:" + authentication.getName();
        }
        return "ip:" + getClientIp(request);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }
}
