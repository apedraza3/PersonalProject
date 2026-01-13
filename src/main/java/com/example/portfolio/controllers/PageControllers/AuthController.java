package com.example.portfolio.controllers.PageControllers;

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

    // -----------------------
    // REGISTER
    // -----------------------
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserRegisterDto body) {
        // 1. Create user and get back a DTO
        UserResponseDto userDto = userService.register(body);

        // 2. Generate JWT using the email from the DTO
        String token = jwtUtil.generateToken(userDto.getEmail());

        // 3. Return both the user DTO and the token
        return ResponseEntity.ok(Map.of(
                "user", userDto,
                "token", token));
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

        // 3. Return user DTO + token
        return ResponseEntity.ok(Map.of(
                "user", userDto,
                "token", token));
    }
}