package com.example.portfolio.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor that checks rate limits before allowing requests to proceed
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    public RateLimitInterceptor(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String ipAddress = getClientIpAddress(request);

        // Apply stricter rate limiting to auth endpoints
        if (path.startsWith("/api/auth/") || path.equals("/auth/login") || path.equals("/auth/register")) {
            if (!rateLimitService.tryConsumeAuth(ipAddress)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write(
                    "{\"error\":\"Too many requests. Please try again in 15 minutes.\",\"code\":\"RATE_LIMIT_EXCEEDED\"}"
                );
                return false;
            }
        }

        // Apply general rate limiting to all API endpoints
        if (path.startsWith("/api/")) {
            if (!rateLimitService.tryConsumeApi(ipAddress)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write(
                    "{\"error\":\"Too many requests. Please slow down.\",\"code\":\"RATE_LIMIT_EXCEEDED\"}"
                );
                return false;
            }
        }

        return true;
    }

    /**
     * Get the real client IP address, considering proxies and load balancers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
