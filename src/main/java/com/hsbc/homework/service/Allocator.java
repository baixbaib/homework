package com.hsbc.homework.service;

import com.hsbc.homework.entity.Account;

import java.util.ArrayList;
import java.util.List;

/**

 * 通过等待通知机制，优化账户申请过程
 */
public class Allocator {

    private static Allocator INSTANCE = new Allocator();

    private List<Account> als;

    private Allocator() {
        als = new ArrayList<>();
    }

    public static Allocator getInstance() {
        return INSTANCE;
    }

    // 一次性申请所有资源
    synchronized void apply(Account from, Account to) {
        while (als.contains(from) || als.contains(to)) {
            try {
                wait();
            } catch (Exception e) {
            }
        }
        als.add(from);
        als.add(to);
    }

    // 归还资源
    synchronized void free(Account from, Account to) {
        als.remove(from);
        als.remove(to);
        notifyAll();
    }
}

