package com.example.portfolio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.example.portfolio.models.Account;


public interface AccountRepository extends JpaRepository<Account, Integer>{
    
    List<Account> findByUserId(Integer userId);

}