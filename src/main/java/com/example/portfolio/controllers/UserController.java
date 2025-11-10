package com.example.portfolio.controllers;

import com.example.portfolio.dto.UserRegisterDto;
import com.example.portfolio.dto.UserResponseDto;
import com.example.portfolio.services.UserService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    // GET /users (optional)
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> listAll() {
        return ResponseEntity.ok(userService.listAll());
    }

    // POST /users (register) — optional
    @PostMapping
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRegisterDto body) {
        UserResponseDto created = userService.register(body);
        return ResponseEntity.created(URI.create("/users/" + created.getId())).body(created);
    }
}