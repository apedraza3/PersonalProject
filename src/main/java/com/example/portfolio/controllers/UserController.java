package com.example.portfolio.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.portfolio.models.Account;
import com.example.portfolio.services.AccountService;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final AccountService accountService;

    public UserController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{id}/accounts")
    public ResponseEntity<List<Account>> getAccountsForUser(@PathVariable("id") Integer userId) {
        List<Account> accounts = accountService.getAccountsForUser(userId);
        return ResponseEntity.ok(accounts); // This can be an empty linet if the user has no accounts
    }

}
