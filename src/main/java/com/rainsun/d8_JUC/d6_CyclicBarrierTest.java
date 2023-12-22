package com.rainsun.d8_JUC;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j(topic = "c.d6_CyclicBarrierTest")
public class d6_CyclicBarrierTest {
    public static void main(String[] args) {
        ExecutorService service = Executors.newFixedThreadPool(2);
        CyclicBarrier barrier = new CyclicBarrier(2, ()->{
            log.debug("task1 task2 finish...");
        });

        for (int i = 0; i < 3; i++) {
            service.submit(()->{
                log.debug("task1 begin...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                try {
                    // 初始计数-1
                    barrier.await();
                    log.debug("task1 end...");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            service.submit(()->{
                log.debug("task2 begin...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                try {
                    // 初始计数-1
                    barrier.await();
                    log.debug("task2 end...");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        service.shutdown();
    }
}
