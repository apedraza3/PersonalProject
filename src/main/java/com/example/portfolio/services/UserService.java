package com.example.portfolio.services;

import org.springframework.stereotype.Service;
import com.example.portfolio.models.User;
import com.example.portfolio.repositories.UserRepository;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCrypt;
import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public User register(User newUser) {
        // Hash the password before saving
        String hashPassword = BCrypt.hashpw(newUser.getPassword(), BCrypt.gensalt());
        newUser.setPassword(hashPassword);
        // set timestamps
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());
        // Save the new user
        return userRepo.save(newUser);
    }

    public User login(String email, String password) {
        Optional<User> possibleUser = userRepo.findByEmail(email);
        if (possibleUser.isPresent()) {
            return null;
        }
        User user = possibleUser.get();
        if (!BCrypt.checkpw(password, user.getPassword())) {
            return null;
        }
        return user;
    }

    public User findById(Integer id) {
        return userRepo.findById(id).orElse(null);
    }

    public User findByEmail(String email) {
        return userRepo.findByEmail(email).orElse(null);
    }

    public User save(User user) {
        return userRepo.save(user);
    }

}
