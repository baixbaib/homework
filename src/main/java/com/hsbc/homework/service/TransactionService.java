package com.hsbc.homework.service;

import com.hsbc.homework.dto.TransactionDTO;
import com.hsbc.homework.entity.Account;
import com.hsbc.homework.entity.Transaction;
import com.hsbc.homework.exception.InsufficientBalanceException;
import com.hsbc.homework.exception.ResourceNotFoundException;
import com.hsbc.homework.repository.AccountRepository;
import com.hsbc.homework.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountService accountService;


    @Transactional
    public Transaction createTransaction(TransactionDTO transactionDTO) {
        Account fromAccount = accountService.getAccountById(transactionDTO.getFromAccountId());
        Account toAccount = accountService.getAccountById(transactionDTO.getToAccountId());

        // 方法返回说明此线程申请到转入，转出账户，可以进行锁定
        Allocator.getInstance().apply(fromAccount, toAccount);
        try {
            synchronized (fromAccount) {
                if (fromAccount.getBalance().compareTo(transactionDTO.getAmount()) < 0) {
                    throw new InsufficientBalanceException("Insufficient balance in account: " + fromAccount.getId());
                }
                synchronized (toAccount) {
                    accountService.updateAccountBalance(fromAccount.getId(), transactionDTO.getAmount(), false);
                    accountService.updateAccountBalance(toAccount.getId(), transactionDTO.getAmount(), true);
                    Transaction transaction = Transaction.builder().fromAccount(fromAccount).toAccount(toAccount).amount(transactionDTO.getAmount()).build();
                    Transaction savedTransaction = transactionRepository.save(transaction);
                    return savedTransaction;
                }
            }
        } finally {
            Allocator.getInstance().free(fromAccount, toAccount);
        }
    }

    @Cacheable(value = "transactions", key = "#id")
    public Transaction getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
        return transaction;
    }

    public Page<Transaction> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }

    public Page<Transaction> getTransactionsByAccountId(Long accountId, Pageable pageable) {
        return transactionRepository.findByFromAccount_IdOrToAccount_Id(accountId, accountId, pageable);
    }

    @CacheEvict(value = "transactions", key = "#id")
    @Transactional
    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
        transactionRepository.delete(transaction);
    }
}