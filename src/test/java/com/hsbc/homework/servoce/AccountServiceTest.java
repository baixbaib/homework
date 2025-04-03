package com.hsbc.homework.servoce;

import com.hsbc.homework.dto.AccountDTO;
import com.hsbc.homework.entity.Account;
import com.hsbc.homework.exception.ResourceNotFoundException;
import com.hsbc.homework.repository.AccountRepository;
import com.hsbc.homework.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccount_Success() {
        AccountDTO dto = new AccountDTO();
        dto.setOwner("Test User");
        dto.setBalance(1000d);

        Account account = Account.builder()
                .owner(dto.getOwner())
                .balance(dto.getBalance())
                .build();

        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account result = accountService.createAccount(dto);

        assertNotNull(result);
        assertEquals(dto.getOwner(), result.getOwner());
        assertEquals(dto.getBalance(), result.getBalance());
    }

    @Test
    void getAccountById_Success() {
        Long accountId = 1L;
        Account account = Account.builder()
                .id(accountId)
                .owner("Test User")
                .balance(1000d)
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        Account result = accountService.getAccountById(accountId);

        assertNotNull(result);
        assertEquals(accountId, result.getId());
    }

    @Test
    void getAccountById_NotFound() {
        Long accountId = 1L;
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.getAccountById(accountId));
    }

    @Test
    void getAllAccounts_Success() {
        PageRequest pageable = PageRequest.of(0, 10);
        List<Account> accounts = List.of(
                Account.builder().id(1L).owner("123").build(),
                Account.builder().id(2L).owner("456").build()
        );
        Page<Account> page = new PageImpl<>(accounts, pageable, accounts.size());

        when(accountRepository.findAll(pageable)).thenReturn(page);

        Page<Account> result = accountService.getAllAccounts(pageable);

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void updateAccountBalance_Deposit_Success() {
        Long accountId = 1L;
        Double amount = 500d;
        Account account = Account.builder()
                .id(accountId)
                .balance(1000d)
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account result = accountService.updateAccountBalance(accountId, amount, true);

        assertEquals(1500, result.getBalance());
    }

    @Test
    void updateAccountBalance_Withdraw_Success() {
        Long accountId = 1L;
        Double amount = 500d;
        Account account = Account.builder()
                .id(accountId)
                .balance(1000d)
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account result = accountService.updateAccountBalance(accountId, amount, false);

        assertEquals(500d, result.getBalance());
    }

    @Test
    void updateAccountBalance_InsufficientBalance() {
        Long accountId = 1L;
        Double amount = 1500d;
        Account account = Account.builder()
                .id(accountId)
                .balance(1000d)
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThrows(IllegalArgumentException.class,
                () -> accountService.updateAccountBalance(accountId, amount, false));
    }
}
