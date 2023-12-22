package com.rainsun.d3_synchronized;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.ReentrantLock;

@Slf4j(topic = "c.d10_ReentrantLock")
public class d10_ReentrantLock {
    private static ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) {
        lock.lock();
        try {
            log.debug("enter main");
            method1();
        }finally {
            lock.unlock();
        }
    }

    public static void method1(){
        lock.lock();
        try {
            log.debug("enter method1");
            method2();
        }finally {
            lock.unlock();
        }
    }
    public static void method2(){
        lock.lock();
        try {
            log.debug("enter method2");
        }finally {
            lock.unlock();
        }
    }
}
