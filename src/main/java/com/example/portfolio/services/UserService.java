package com.example.portfolio.services;

import org.springframework.stereotype.Service;
import com.example.portfolio.models.User;
import com.example.portfolio.repositories.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public User findById(Integer id){
        return userRepo.findById(id).orElse(null);
    }

    public User findByEmail(String email){
        return userRepo.findByEmail(email).orElse(null);
    }

    public User save(User user){
        return userRepo.save(user);
    }

}
