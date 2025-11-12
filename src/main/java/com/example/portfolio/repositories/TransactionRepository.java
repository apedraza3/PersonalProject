package com.example.portfolio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.example.portfolio.models.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    List<Transaction> findByAccountId(Integer accountId);

    // this gives an optional filter by date range
    Page<Transaction> findByAccountIdAndDateBetween(Integer accountId, LocalDate from, LocalDate to, Pageable pageable);

    Page<Transaction> findByAccountId(Integer accountId, Pageable pageable);

    // Account balannce
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.account.id = :accountId")
    BigDecimal SUmAmountByAccountId(@Param("accountId") Integer accountId);

}