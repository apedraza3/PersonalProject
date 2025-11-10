package com.example.portfolio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.portfolio.models.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmailIgnoreCase(String email);
}
