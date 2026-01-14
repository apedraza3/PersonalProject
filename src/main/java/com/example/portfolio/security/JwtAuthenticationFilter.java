package com.example.portfolio.security;

import com.example.portfolio.models.User;
import com.example.portfolio.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepo;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepo) {
        this.jwtUtil = jwtUtil;
        this.userRepo = userRepo;
    }

    /**
     * Extract JWT token from request - checks both Cookie and Authorization header
     * Cookie is preferred for web app, Authorization header for API clients
     */
    private String extractToken(HttpServletRequest request) {
        // 1. Try to get from cookie first (HttpOnly secure cookie)
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // 2. Fallback to Authorization header (for API clients/mobile apps)
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }

        return null;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null) {
            try {
                String email = jwtUtil.getSubject(token);
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Load user
                    User u = userRepo.findByEmail(email).orElse(null);
                    if (u != null) {
                        var auth = new UsernamePasswordAuthenticationToken(
                                email, // principal
                                null, // no credentials
                                Collections.emptyList() // roles
                        );
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (Exception e) {
                // Invalid/expired token → leave context unauthenticated
            }
        }

        filterChain.doFilter(request, response);
    }
}