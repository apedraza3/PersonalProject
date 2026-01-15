package com.example.portfolio.controllers.ApiController;

import com.example.portfolio.dto.UserRegisterDto;
import com.example.portfolio.dto.UserResponseDto;
import com.example.portfolio.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.Map;
import com.example.portfolio.security.CurrentUser;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CurrentUser currentUser;

    public UserController(UserService userService, CurrentUser currentUser) {
        this.userService = userService;
        this.currentUser = currentUser;
    }

    // GET /users/{id} - Only allow users to view their own profile
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        String email = currentUser.email();
        if (email == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }

        UserResponseDto requestedUser = userService.getById(id);
        UserResponseDto currentUserDto = userService.getByEmail(email);

        // Only allow viewing own profile
        if (!currentUserDto.getId().equals(id)) {
            return ResponseEntity.status(403).body("Access denied: you can only view your own profile");
        }

        return ResponseEntity.ok(requestedUser);
    }

    // POST /users (register) — optional
    @PostMapping
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRegisterDto body) {
        UserResponseDto created = userService.register(body);
        return ResponseEntity.created(URI.create("/users/" + created.getId())).body(created);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> me() {
        String email = currentUser.email();
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(userService.getByEmail(email));
    }

    /**
     * DELETE /api/users/me - Delete current user account and all associated data
     * This implements GDPR "right to be forgotten"
     *
     * Deletes:
     * - User account
     * - All PlaidItems (encrypted access tokens)
     * - All Accounts
     * - All Transactions (cascade via Account deletion)
     */
    @DeleteMapping("/me")
    public ResponseEntity<?> deleteAccount() {
        String email = currentUser.email();
        if (email == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Not authenticated"));
        }

        UserResponseDto user = userService.getByEmail(email);
        userService.deleteUserAndAllData(user.getId());

        return ResponseEntity.ok()
                .body(Map.of("message", "Account successfully deleted"));
    }
}