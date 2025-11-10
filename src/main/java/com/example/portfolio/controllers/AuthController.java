package com.example.portfolio.controllers;

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

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRegisterDto body) {
        UserResponseDto created = userService.register(body);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {

        String email = body.getOrDefault("email", "").trim().toLowerCase();
        String password = body.getOrDefault("password", "").trim();

        if (email.isEmpty() || password.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required"));
        }

        var userDto = userService.login(email, password);
        if (userDto == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid email or password"));
        }
        String token = jwtUtil.generateToken(userDto.getEmail());
        return ResponseEntity.ok(Map.of("user", userDto, "token", token));
    }
}
