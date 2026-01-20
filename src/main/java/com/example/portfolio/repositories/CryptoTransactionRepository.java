package com.example.portfolio.repositories;

import com.example.portfolio.models.CryptoTransaction;
import com.example.portfolio.models.CryptoWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CryptoTransactionRepository extends JpaRepository<CryptoTransaction, Integer> {

    /**
     * Find transaction by blockchain hash (for idempotent sync)
     */
    Optional<CryptoTransaction> findByTxHash(String txHash);

    /**
     * Find all transactions for a specific wallet
     */
    List<CryptoTransaction> findByWallet(CryptoWallet wallet);

    /**
     * Find all transactions for a specific wallet, ordered by date descending
     */
    List<CryptoTransaction> findByWalletOrderByDateDesc(CryptoWallet wallet);

    /**
     * Find all transactions for all wallets owned by a user
     */
    @Query("SELECT ct FROM CryptoTransaction ct WHERE ct.wallet.owner.id = :userId ORDER BY ct.date DESC")
    List<CryptoTransaction> findByUserId(Integer userId);

    /**
     * Check if transaction already exists (prevent duplicates)
     */
    boolean existsByTxHash(String txHash);

    /**
     * Delete all transactions for a wallet
     */
    void deleteByWallet_Id(Integer walletId);
}
