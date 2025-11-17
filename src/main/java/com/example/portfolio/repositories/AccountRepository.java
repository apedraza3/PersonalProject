package com.example.portfolio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.example.portfolio.models.Account;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Integer> {

    List<Account> findByUserId(Integer userId);

    List<Account> findByOwnerId(Integer ownerId);

    Optional<Account> findByPlaidAccountId(String plaidAccountId);

}