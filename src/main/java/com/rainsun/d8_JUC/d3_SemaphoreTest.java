package com.rainsun.d8_JUC;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Semaphore;

@Slf4j(topic = "c.d3_SemaphoreTest")
public class d3_SemaphoreTest {
    public static void main(String[] args) {
        // 1. 创建 semaphore 对象
        Semaphore semaphore = new Semaphore(3);

        // 2. 10 线程同时执行
        for (int i = 0; i < 10; i++) {
            new Thread(()->{
                // 3. acquire 获得许可
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                try {
                    log.debug("running ... ");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    log.debug("end...");
                } finally {
                    // 4. 释放许可
                    semaphore.release();
                }
            }).start();
        }
    }
}
