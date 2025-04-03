package com.hsbc.homework.controller;

import com.hsbc.homework.dto.AccountDTO;
import com.hsbc.homework.entity.Account;
import com.hsbc.homework.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Management", description = "Manage bank accounts")
public class AccountController {
    @Autowired
    private AccountService accountService;

    @PostMapping
    @Operation(summary = "Create a new account")
    public ResponseEntity<Account> createAccount(@Valid @RequestBody AccountDTO accountDTO) {
        Account account = accountService.createAccount(accountDTO);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID")
    public ResponseEntity<Account> getAccountById(@PathVariable Long id) {
        Account account = accountService.getAccountById(id);
        return ResponseEntity.ok(account);
    }

    @GetMapping
    @Operation(summary = "Get all accounts (paginated)")
    public ResponseEntity<Page<Account>> getAllAccounts(Pageable pageable) {
        Page<Account> accounts = accountService.getAllAccounts(pageable);
        return ResponseEntity.ok(accounts);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a Account")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}
