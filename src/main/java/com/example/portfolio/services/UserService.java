package com.example.portfolio.services;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.example.portfolio.dto.UserRegisterDto;
import com.example.portfolio.dto.UserResponseDto;
import com.example.portfolio.mappers.UserMapper;
import com.example.portfolio.models.User;
import com.example.portfolio.repositories.UserRepository;
import com.example.portfolio.repositories.PlaidItemRepository;
import com.example.portfolio.repositories.AccountRepository;
import com.example.portfolio.repositories.RefreshTokenRepository;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepo;
    private final PlaidItemRepository plaidItemRepo;
    private final AccountRepository accountRepo;
    private final RefreshTokenRepository refreshTokenRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(
            UserRepository userRepo,
            PlaidItemRepository plaidItemRepo,
            AccountRepository accountRepo,
            RefreshTokenRepository refreshTokenRepo) {
        this.userRepo = userRepo;
        this.plaidItemRepo = plaidItemRepo;
        this.accountRepo = accountRepo;
        this.refreshTokenRepo = refreshTokenRepo;
    }

    // READ: get by id
    @Transactional(readOnly = true)
    public UserResponseDto getById(Integer id) {
        User u = userRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return UserMapper.toResponseDto(u);
    }

    // READ: list all (if you need it)
    @Transactional(readOnly = true)
    public List<UserResponseDto> listAll() {
        return userRepo.findAll().stream().map(UserMapper::toResponseDto).toList();
    }

    // REGISTER (stores password as-is unless you add hashing)
    public UserResponseDto register(UserRegisterDto dto) {
        userRepo.findByEmail(dto.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("Email already registered");
        });

        User u = new User();
        u.setName(dto.getName());
        u.setEmail(dto.getEmail());

        String hash = encoder.encode(dto.getPassword());
        u.setPassword(hash);

        u.setCreatedAt(LocalDateTime.now());
        u.setUpdatedAt(LocalDateTime.now());

        User saved = userRepo.save(u);
        return UserMapper.toResponseDto(saved);
    }

    // LOGIN
    @Transactional(readOnly = true)
    public UserResponseDto login(String email, String password) {

        var opt = userRepo.findByEmail(email);
        if (opt.isEmpty())
            return null;

        var u = opt.get();
        String stored = u.getPassword();

        if (stored == null || !encoder.matches(password, stored)) {
            return null;
        }

        return UserMapper.toResponseDto(u);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getByEmail(String email) {
        var u = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return UserMapper.toResponseDto(u);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    /**
     * Deletes all user data including:
     * - Transactions (via cascade from Accounts)
     * - Accounts (child of User)
     * - PlaidItems (child of User, with encrypted access tokens)
     * - RefreshTokens (child of User)
     * - User account
     *
     * This is for GDPR compliance / right to be forgotten
     *
     * Deletion order respects FK constraints:
     * 1. Children of Accounts (Transactions) - handled by DB CASCADE
     * 2. Children of Users (Accounts, PlaidItems, RefreshTokens)
     * 3. User itself
     */
    public void deleteUserAndAllData(Integer userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Delete in FK-safe order: children first, then parent
        // Note: Transactions are automatically deleted via CASCADE when Accounts are deleted

        // 1. Delete accounts (this cascades to transactions via DB constraint)
        accountRepo.deleteByUserId(userId);

        // 2. Delete Plaid items (no children, safe to delete)
        plaidItemRepo.deleteByOwner_Id(userId);

        // 3. Delete refresh tokens (no children, safe to delete)
        refreshTokenRepo.deleteByUserId(userId);

        // 4. Delete the user account itself (all children are now gone)
        userRepo.delete(user);
    }
}