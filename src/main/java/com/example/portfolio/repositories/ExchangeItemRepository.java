package com.example.portfolio.repositories;

import com.example.portfolio.models.ExchangeItem;
import com.example.portfolio.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeItemRepository extends JpaRepository<ExchangeItem, Integer> {

    /**
     * Find all exchange connections for a specific user
     */
    List<ExchangeItem> findByOwner(User owner);

    /**
     * Find all exchange connections for a specific user by user ID
     */
    List<ExchangeItem> findByOwner_Id(Integer userId);

    /**
     * Find a specific exchange connection for a user
     */
    Optional<ExchangeItem> findByOwner_IdAndExchange(Integer userId, String exchange);

    /**
     * Check if user has already connected an exchange
     */
    boolean existsByOwner_IdAndExchange(Integer userId, String exchange);

    /**
     * Find exchange connections by exchange type
     */
    List<ExchangeItem> findByExchange(String exchange);

    /**
     * Delete all exchange connections for a user (for user deletion cascade)
     */
    void deleteByOwner_Id(Integer userId);
}
