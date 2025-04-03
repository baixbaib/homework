package com.hsbc.homework.repository;

import com.hsbc.homework.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByFromAccount_IdOrToAccount_Id(Long fromAccountId, Long toAccountId, Pageable pageable);

    void deleteByFromAccountIdOrToAccountId(Long fromAccountId, Long toAccountId);

}
