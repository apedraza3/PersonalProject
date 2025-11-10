package com.example.portfolio.security;

import com.example.portfolio.models.User;
import com.example.portfolio.repositories.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            try {
                String email = jwtUtil.getSubject(token);
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Load user (optional: check isActive, etc.)
                    User u = userRepo.findByEmailIgnoreCase(email).orElse(null);
                    if (u != null) {
                        var auth = new UsernamePasswordAuthenticationToken(
                                email, // principal (keep it simple: email string)
                                null, // no credentials
                                Collections.emptyList() // roles if you add them later
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