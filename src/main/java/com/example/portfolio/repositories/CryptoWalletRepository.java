package com.example.portfolio.repositories;

import com.example.portfolio.models.CryptoWallet;
import com.example.portfolio.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CryptoWalletRepository extends JpaRepository<CryptoWallet, Integer> {

    /**
     * Find all wallets for a specific user
     */
    List<CryptoWallet> findByOwner(User owner);

    /**
     * Find all wallets for a specific user by user ID
     */
    List<CryptoWallet> findByOwner_Id(Integer userId);

    /**
     * Find wallets by blockchain type for a user
     */
    List<CryptoWallet> findByOwner_IdAndBlockchain(Integer userId, String blockchain);

    /**
     * Find wallet by address (to prevent duplicates)
     */
    Optional<CryptoWallet> findByWalletAddressAndOwner_Id(String walletAddress, Integer userId);

    /**
     * Check if wallet exists for user
     */
    boolean existsByWalletAddressAndOwner_Id(String walletAddress, Integer userId);

    /**
     * Delete all wallets for a user (for user deletion cascade)
     */
    void deleteByOwner_Id(Integer userId);
}
