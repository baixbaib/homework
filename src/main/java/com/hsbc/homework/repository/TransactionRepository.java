package com.hsbc.homework.repository;

import com.hsbc.homework.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByFromAccountId(Long fromAccountId);

    List<Transaction> findByToAccountId(Long toAccountId);

    Page<Transaction> findByFromAccount_IdOrToAccount_Id(Long fromAccountId, Long toAccountId, Pageable pageable);

    void deleteByFromAccountIdOrToAccountId(Long fromAccountId, Long toAccountId);

}
