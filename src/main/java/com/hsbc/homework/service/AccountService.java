package com.hsbc.homework.service;

import com.hsbc.homework.dto.AccountDTO;
import com.hsbc.homework.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountService {
    Account createAccount(AccountDTO accountDTO);

    Account getAccountById(Long id);

    Page<Account> getAllAccounts(Pageable pageable);

    Account updateAccountBalance(Long id, double amount, boolean isDeposit);

    void deleteAccount(Long id);
}
