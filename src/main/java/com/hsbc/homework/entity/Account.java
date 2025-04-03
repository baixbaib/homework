package com.hsbc.homework.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Account owner is required")
    private String owner;

    @NotNull(message = "Balance is required")
    private Double balance;


    public void deposit(double amount) {
        this.balance += amount;
    }

    public void withdraw(double amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        this.balance -= amount;
    }

//    @OneToMany
//    @JoinTable(
//            name = "CUSTOMER_ORDER",  // 中间表名
//            joinColumns = @JoinColumn(name = "CUSTOMER_ID"),  // 当前实体ID列
//            inverseJoinColumns = @JoinColumn(name = "ORDER_ID")  // 关联实体ID列
//    )
//    private List<Transaction> transactions;

}
