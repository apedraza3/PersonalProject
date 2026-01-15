package com.example.portfolio.controllers.PageControllers;

import com.example.portfolio.models.RefreshToken;
import com.example.portfolio.models.User;
import com.example.portfolio.security.RateLimitService;
import com.example.portfolio.services.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import com.example.portfolio.dto.UserRegisterDto;
import com.example.portfolio.dto.UserResponseDto;
import com.example.portfolio.services.UserService;
import jakarta.validation.Valid;
import com.example.portfolio.security.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RateLimitService rateLimitService;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.cookies.secure:false}")
    private boolean secureCookies;

    public AuthController(
            UserService userService,
            JwtUtil jwtUtil,
            RateLimitService rateLimitService,
            RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.rateLimitService = rateLimitService;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Create an HttpOnly secure cookie with the JWT access token (short-lived: 15 min)
     */
    private ResponseCookie createJwtCookie(String token) {
        return ResponseCookie.from("jwt", token)
                .httpOnly(true)  // Prevents JavaScript access (XSS protection)
                .secure(secureCookies)  // Controlled by app.cookies.secure property
                .path("/")
                .maxAge(15 * 60)  // 15 minutes (matches JWT TTL)
                .sameSite("Lax")  // CSRF protection
                .build();
    }

    /**
     * Create an HttpOnly secure cookie with the refresh token (long-lived: 7 days)
     */
    private ResponseCookie createRefreshTokenCookie(String token) {
        return ResponseCookie.from("refresh_token", token)
                .httpOnly(true)  // Prevents JavaScript access
                .secure(secureCookies)  // Controlled by app.cookies.secure property
                .path("/api/auth")   // Updated to match new controller path
                .maxAge(7 * 24 * 60 * 60)  // 7 days
                .sameSite("Lax")
                .build();
    }

    // -----------------------
    // REGISTER
    // -----------------------
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserRegisterDto body) {
        // 1. Create user and get back a DTO
        UserResponseDto userDto = userService.register(body);

        // 2. Get the user entity for refresh token creation
        User user = userService.findByEmail(userDto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Generate short-lived JWT (15 min)
        String accessToken = jwtUtil.generateToken(userDto.getEmail());

        // 4. Generate long-lived refresh token (7 days)
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // 5. Set both tokens in HttpOnly cookies
        ResponseCookie jwtCookie = createJwtCookie(accessToken);
        ResponseCookie refreshCookie = createRefreshTokenCookie(refreshToken.getToken());

        // 6. Return user DTO only (tokens are in cookies)
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(Map.of("user", userDto));
    }

    // -----------------------
    // LOGIN
    // -----------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletRequest request) {

        String email = body.getOrDefault("email", "").trim().toLowerCase();
        String password = body.getOrDefault("password", "").trim();

        if (email.isEmpty() || password.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required"));
        }

        // 1. Authenticate and get back a DTO
        UserResponseDto userDto = userService.login(email, password);

        if (userDto == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid email or password"));
        }

        // 2. Reset rate limit on successful login (reward good behavior)
        String ipAddress = getClientIpAddress(request);
        rateLimitService.reset("auth:" + ipAddress);

        // 3. Get the user entity for refresh token creation
        User user = userService.findByEmail(userDto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 4. Generate short-lived JWT (15 min)
        String accessToken = jwtUtil.generateToken(userDto.getEmail());

        // 5. Generate long-lived refresh token (7 days)
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // 6. Set both tokens in HttpOnly cookies
        ResponseCookie jwtCookie = createJwtCookie(accessToken);
        ResponseCookie refreshCookie = createRefreshTokenCookie(refreshToken.getToken());

        // 7. Return user DTO only (tokens are in cookies)
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(Map.of("user", userDto));
    }

    /**
     * Get the real client IP address, considering proxies and load balancers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    // -----------------------
    // REFRESH TOKEN
    // -----------------------
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        // Get refresh token from cookie
        String refreshTokenValue = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshTokenValue = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshTokenValue == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Refresh token not found"));
        }

        // Verify refresh token
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenValue)
                .orElse(null);

        if (refreshToken == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid or expired refresh token"));
        }

        // Generate new access token
        String newAccessToken = jwtUtil.generateToken(refreshToken.getUser().getEmail());
        ResponseCookie jwtCookie = createJwtCookie(newAccessToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(Map.of("message", "Token refreshed successfully"));
    }

    // -----------------------
    // LOGOUT
    // -----------------------
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // Revoke refresh token if present
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    String refreshTokenValue = cookie.getValue();
                    refreshTokenService.findByToken(refreshTokenValue)
                            .ifPresent(refreshTokenService::revokeToken);
                    break;
                }
            }
        }

        // Clear both cookies
        ResponseCookie jwtCookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(secureCookies)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(secureCookies)
                .path("/api/auth")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(Map.of("message", "Logged out successfully"));
    }
}