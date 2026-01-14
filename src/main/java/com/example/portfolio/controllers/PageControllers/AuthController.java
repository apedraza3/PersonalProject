package com.example.portfolio.controllers.PageControllers;

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
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Create an HttpOnly secure cookie with the JWT token
     */
    private ResponseCookie createJwtCookie(String token) {
        return ResponseCookie.from("jwt", token)
                .httpOnly(true)  // Prevents JavaScript access (XSS protection)
                .secure(false)    // Set to true in production with HTTPS
                .path("/")
                .maxAge(24 * 60 * 60)  // 24 hours
                .sameSite("Lax")  // CSRF protection
                .build();
    }

    // -----------------------
    // REGISTER
    // -----------------------
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserRegisterDto body) {
        // 1. Create user and get back a DTO
        UserResponseDto userDto = userService.register(body);

        // 2. Generate JWT using the email from the DTO
        String token = jwtUtil.generateToken(userDto.getEmail());

        // 3. Set JWT in HttpOnly cookie
        ResponseCookie jwtCookie = createJwtCookie(token);

        // 4. Return user DTO only (token is in cookie)
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(Map.of("user", userDto));
    }

    // -----------------------
    // LOGIN
    // -----------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {

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

        // 2. Generate JWT using the email from the DTO
        String token = jwtUtil.generateToken(userDto.getEmail());

        // 3. Set JWT in HttpOnly cookie
        ResponseCookie jwtCookie = createJwtCookie(token);

        // 4. Return user DTO only (token is in cookie)
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(Map.of("user", userDto));
    }

    // -----------------------
    // LOGOUT
    // -----------------------
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Clear the JWT cookie
        ResponseCookie jwtCookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false)  // Set to true in production
                .path("/")
                .maxAge(0)  // Expire immediately
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(Map.of("message", "Logged out successfully"));
    }
}