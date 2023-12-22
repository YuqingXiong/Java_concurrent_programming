package com.rainsun.d3_synchronized;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j(topic = "c.d12_TestConditionLock")
public class d12_TestConditionLock {
    static ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) throws InterruptedException {
        Condition condition1 = lock.newCondition();
        Condition condition2 = lock.newCondition();

        lock.lock();
        // 进入条件1（休息室1）等待
        condition1.await();

        // 叫醒休息室1中的线程：
        condition1.signal();
        condition1.signalAll();
    }
}
