package com.hsbc.homework.servoce;

import com.hsbc.homework.dto.TransactionDTO;
import com.hsbc.homework.entity.Account;
import com.hsbc.homework.entity.Transaction;
import com.hsbc.homework.exception.InsufficientBalanceException;
import com.hsbc.homework.exception.ResourceNotFoundException;
import com.hsbc.homework.repository.AccountRepository;
import com.hsbc.homework.repository.TransactionRepository;
import com.hsbc.homework.service.AccountService;
import com.hsbc.homework.service.TransactionService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void createTransaction_Success() {
        Long fromAccountId = 1L;
        Long toAccountId = 2L;
        Double amount = 500d;

        Account fromAccount = Account.builder()
                .id(fromAccountId)
                .owner("123")
                .balance(1000d)
                .build();

        Account toAccount = Account.builder()
                .id(toAccountId)
                .owner("456")
                .balance(500d)
                .build();

        TransactionDTO dto = new TransactionDTO();
        dto.setFromAccountId(fromAccountId);
        dto.setToAccountId(toAccountId);
        dto.setAmount(amount);

        when(accountService.getAccountById(fromAccountId)).thenReturn(fromAccount);
        when(accountService.getAccountById(toAccountId)).thenReturn(toAccount);
        when(accountService.updateAccountBalance(eq(fromAccountId), any(Double.class), eq(false)))
                .thenAnswer(inv -> {
                    fromAccount.setBalance(fromAccount.getBalance() + (Double) inv.getArgument(1));
                    return fromAccount;
                });
        when(accountService.updateAccountBalance(eq(toAccountId), any(Double.class), eq(true)))
                .thenAnswer(inv -> {
                    toAccount.setBalance(toAccount.getBalance() - (Double) inv.getArgument(1));
                    return toAccount;
                });
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        var result = transactionService.createTransaction(dto);

        assertNotNull(result);
        assertEquals(fromAccountId, result.getFromAccount().getId());
        assertEquals(toAccountId, result.getToAccount().getId());
        assertEquals(amount, result.getAmount());

        verify(accountService, times(1)).updateAccountBalance(fromAccountId, amount, false);
        verify(accountService, times(1)).updateAccountBalance(toAccountId, amount, true);
    }

    @Test
    void createTransaction_InsufficientBalance() {
        Long fromAccountId = 1L;
        Long toAccountId = 2L;
        Double amount = 1500d;

        Account fromAccount = Account.builder()
                .id(fromAccountId)
                .owner("123")
                .balance(1000d)
                .build();

        TransactionDTO dto = new TransactionDTO();
        dto.setFromAccountId(fromAccountId);
        dto.setToAccountId(toAccountId);
        dto.setAmount(amount);

        when(accountService.getAccountById(fromAccountId)).thenReturn(fromAccount);

        assertThrows(InsufficientBalanceException.class, () -> transactionService.createTransaction(dto));
    }

    @Test
    void getTransactionById_Success() {
        Long transactionId = 1L;
        Long fromAccountId = 1L;
        Long toAccountId = 2L;

        Account fromAccount = Account.builder()
                .id(fromAccountId)
                .owner("123")
                .build();

        Account toAccount = Account.builder()
                .id(toAccountId)
                .owner("456")
                .build();

        Transaction transaction = Transaction.builder()
                .id(transactionId)
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(500d)
                .build();

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        var result = transactionService.getTransactionById(transactionId);

        assertNotNull(result);
        assertEquals(transactionId, result.getId());
        assertEquals(fromAccountId, result.getFromAccount().getId());
        assertEquals(toAccountId, result.getToAccount().getId());
    }

    @Test
    void getTransactionById_NotFound() {
        Long transactionId = 1L;
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.getTransactionById(transactionId));
    }

    @Test
    void getAllTransactions_Success() {
        PageRequest pageable = PageRequest.of(0, 10);
        List<Transaction> transactions = List.of(
                Transaction.builder().id(1L).build(),
                Transaction.builder().id(2L).build()
        );
        Page<Transaction> page = new PageImpl<>(transactions, pageable, transactions.size());

        when(transactionRepository.findAll(pageable)).thenReturn(page);

        Page<?> result = transactionService.getAllTransactions(pageable);

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void deleteTransaction_Success() {
        Long transactionId = 1L;
        Long fromAccountId = 1L;
        Long toAccountId = 2L;

        Account fromAccount = Account.builder()
                .id(fromAccountId)
                .owner("123")
                .balance(500d)
                .build();

        Account toAccount = Account.builder()
                .id(toAccountId)
                .owner("456")
                .balance(1500d)
                .build();

        Transaction transaction = Transaction.builder()
                .id(transactionId)
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(500d)
                .build();

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));


        transactionService.deleteTransaction(transactionId);

        verify(transactionRepository, times(1)).delete(transaction);
    }
}
