package com.hsbc.homework.repository;

import com.hsbc.homework.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    // 根据所有者查找账户n
    Optional<Account> findByOwner(String owner);
}
