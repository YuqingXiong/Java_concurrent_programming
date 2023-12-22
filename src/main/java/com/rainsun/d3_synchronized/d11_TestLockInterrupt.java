package com.rainsun.d3_synchronized;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.ReentrantLock;

@Slf4j(topic = "c.d11_TestLockInterrupt")
public class d11_TestLockInterrupt {
    private static ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(()->{
            try {
                log.debug("尝试获得锁");
                // lockInterruptibly：
                //  如果没有竞争，此方法会获取lock对象的锁
                //  如果有竞争，则进去阻塞队列，可以被其他线程用interrupt方法打断
                lock.lockInterruptibly();
            }catch (InterruptedException e){
                e.printStackTrace();
                log.debug("没有获得锁，返回");
                return;
            }

            try {
                log.debug("获取到锁");
            }finally {
                lock.unlock();
            }
        }, "t1");

        lock.lock(); // 主线程先获取锁，t1等待
        t1.start();

        Thread.sleep(1000);
        log.debug("打断 t1");
        t1.interrupt();


    }

}
