package com.hsbc.homework.service.impl;

import com.hsbc.homework.dto.AccountDTO;
import com.hsbc.homework.entity.Account;
import com.hsbc.homework.exception.ResourceExistedException;
import com.hsbc.homework.exception.ResourceNotFoundException;
import com.hsbc.homework.repository.AccountRepository;
import com.hsbc.homework.repository.TransactionRepository;
import com.hsbc.homework.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional
    @Override
    public synchronized Account createAccount(AccountDTO accountDTO) {
        boolean existedAccount = accountRepository.findByOwner(accountDTO.getOwner()).isPresent();
        if (existedAccount) {
            throw new ResourceExistedException("account has existed");
        }
        Account account = Account.builder().owner(accountDTO.getOwner()).balance(accountDTO.getBalance()).build();
        return accountRepository.save(account);
    }

    @Cacheable(value = "accounts", key = "#id")
    @Override
    public Account getAccountById(Long id) {
        return accountRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
    }

    @Override
    public Page<Account> getAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable);
    }

    @CachePut(value = "accounts", key = "#id")
    @Transactional
    @Override
    public Account updateAccountBalance(Long id, double amount, boolean isDeposit) {
        Account account = getAccountById(id);
        if (isDeposit) {
            account.deposit(amount);
        } else {
            account.withdraw(amount);
        }
        return accountRepository.save(account);
    }

    // 删除账户
    @Transactional
    @CacheEvict(value = "accounts", key = "#id")
    @Override
    public void deleteAccount(Long id) {
        Account account = getAccountById(id);
        transactionRepository.deleteByFromAccountIdOrToAccountId(id, id);
        accountRepository.delete(account);
    }
}
