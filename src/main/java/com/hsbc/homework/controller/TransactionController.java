package com.hsbc.homework.controller;


import com.hsbc.homework.dto.TransactionDTO;
import com.hsbc.homework.entity.Transaction;
import com.hsbc.homework.service.TransactionService;
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
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "Manage financial transactions")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Create a new transaction")
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody TransactionDTO transactionDTO) {
        Transaction response = transactionService.createTransaction(transactionDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        Transaction response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all transactions (paginated)")
    public ResponseEntity<Page<Transaction>> getAllTransactions(Pageable pageable) {
        Page<Transaction> responses = transactionService.getAllTransactions(pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get transactions by account ID (paginated)")
    public ResponseEntity<Page<Transaction>> getTransactionsByAccountId(
            @PathVariable Long accountId, Pageable pageable) {
        Page<Transaction> responses = transactionService.getTransactionsByAccountId(accountId, pageable);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transaction")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}