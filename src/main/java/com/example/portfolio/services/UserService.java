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
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
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
    @Transactional
    public UserResponseDto login(String email, String password) {

        var opt = userRepo.findByEmail(email);
        if (opt.isEmpty())
            return null;

        var u = opt.get();
        String stored = u.getPassword();
        boolean authenticated = false;

        if (stored != null && stored.startsWith("$2a$") || (stored != null && stored.startsWith("$2b$"))) {
            authenticated = encoder.matches(password, stored);
        } else {
            authenticated = stored != null && stored.equals(password);
            if (authenticated) {
                String newHash = encoder.encode(password);
                u.setPassword(newHash);
                u.setUpdatedAt(LocalDateTime.now());
                userRepo.save(u);
            }
        }
        if (!authenticated)
            return null;

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
}