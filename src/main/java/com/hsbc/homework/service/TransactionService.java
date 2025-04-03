package com.hsbc.homework.service;

import com.hsbc.homework.dto.TransactionDTO;
import com.hsbc.homework.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {
    Transaction createTransaction(TransactionDTO transactionDTO);

    Transaction getTransactionById(Long id);

    Page<Transaction> getAllTransactions(Pageable pageable);

    Page<Transaction> getTransactionsByAccountId(Long accountId, Pageable pageable);

    void deleteTransaction(Long id);

    Transaction updateTransaction(TransactionDTO transactionDTO);

}
