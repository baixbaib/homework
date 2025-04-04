package com.hsbc.homework.service.impl;

import com.hsbc.homework.dto.TransactionDTO;
import com.hsbc.homework.entity.Account;
import com.hsbc.homework.entity.Transaction;
import com.hsbc.homework.exception.InsufficientBalanceException;
import com.hsbc.homework.exception.ResourceNotFoundException;
import com.hsbc.homework.repository.TransactionRepository;
import com.hsbc.homework.service.AccountService;
import com.hsbc.homework.service.Allocator;
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

public class TransactionServiceImpl implements com.hsbc.homework.service.TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountService accountService;


    @Transactional
    @Override
    public Transaction createTransaction(TransactionDTO transactionDTO) {
        Account fromAccount = accountService.getAccountById(transactionDTO.getFromAccountId());
        Account toAccount = accountService.getAccountById(transactionDTO.getToAccountId());

        // 方法返回说明此线程申请到转入，转出账户，可以进行锁定
        // 尽管同一条记录多次查询出的fromAccount, toAccount对象不是同一个对象。但是 Allocator.getInstance().apply方法中有List.contains操作，contains操作是根据equals方法进行判断的。
        // 所以一个账户被apply后，其他线程查询到的此账户的其他java对象，也不会被apply到，不会影响到后续的交易操作，出现并发问题。
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
    @Override
    public Transaction getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
        return transaction;
    }

    @Override
    public Page<Transaction> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }

    @Override
    public Page<Transaction> getTransactionsByAccountId(Long accountId, Pageable pageable) {
        return transactionRepository.findByFromAccount_IdOrToAccount_Id(accountId, accountId, pageable);
    }

    @CacheEvict(value = "transactions", key = "#id")
    @Transactional
    @Override
    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
        transactionRepository.delete(transaction);
    }

    @Override
    public Transaction updateTransaction(TransactionDTO transactionDTO) {
        Transaction originalRecord = getTransactionById(transactionDTO.getId());
        if (!originalRecord.getFromAccount().getId().equals(transactionDTO.getFromAccountId()) || !originalRecord.getToAccount().getId().equals(transactionDTO.getToAccountId())) {
            throw new ResourceNotFoundException("Transaction not found with id: " + transactionDTO.getId() + " and from account: " + transactionDTO.getFromAccountId() + " and to account: " + transactionDTO.getToAccountId());
        }

        Account fromAccount = accountService.getAccountById(transactionDTO.getFromAccountId());
        Account toAccount = accountService.getAccountById(transactionDTO.getToAccountId());

        // 方法返回说明此线程申请到转入，转出账户，可以进行锁定
        // 尽管同一条记录多次查询出的fromAccount, toAccount对象不是同一个对象。但是 Allocator.getInstance().apply方法中有List.contains操作，contains操作是根据equals方法进行判断的。
        // 所以一个账户被apply后，其他线程查询到的此账户的其他java对象，也不会被apply到，不会影响到后续的交易操作，出现并发问题。
        Allocator.getInstance().apply(fromAccount, toAccount);
        try {
            synchronized (fromAccount) {

                synchronized (toAccount) {
                    double diff = transactionDTO.getAmount() - originalRecord.getAmount();
                    // 新交易金额比原始金额数量大，转出账户需要继续转账，查看转出账户余额是否充足
                    if (diff > 0) {
                        if (fromAccount.getBalance().compareTo(diff) < 0) {
                            throw new InsufficientBalanceException("Insufficient balance in account: " + fromAccount.getId());
                        }
                    } else {
                        // 说明转入账户需要退还钱
                        if (toAccount.getBalance().compareTo(Math.abs(diff)) < 0) {
                            throw new InsufficientBalanceException("Insufficient balance in account: " + toAccount.getId());
                        }
                    }

                    boolean fromAccountIsDeposit = diff > 0 ? false : true;
                    boolean toAccountIsDeposit = diff > 0 ? true : false;
                    accountService.updateAccountBalance(fromAccount.getId(), Math.abs(diff), fromAccountIsDeposit);
                    accountService.updateAccountBalance(toAccount.getId(), Math.abs(diff), toAccountIsDeposit);
                    Transaction transaction = Transaction.builder().id(transactionDTO.getId()).fromAccount(fromAccount).toAccount(toAccount).amount(transactionDTO.getAmount()).build();
                    Transaction updatedTransaction = transactionRepository.save(transaction);
                    return updatedTransaction;
                }
            }
        } finally {
            Allocator.getInstance().free(fromAccount, toAccount);
        }
    }
}