package com.example.portfolio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.example.portfolio.models.Transaction;


public interface TransactionRepository extends JpaRepository<Transaction, Integer>{
    
    List<Transaction> findByAccountId(Integer accountId);

}