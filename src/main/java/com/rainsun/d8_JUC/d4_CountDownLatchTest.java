package com.rainsun.d8_JUC;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j(topic = "c.d4_CountDownLatchTest")
public class d4_CountDownLatchTest {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(4);
        CountDownLatch countDownLatch = new CountDownLatch(3);

        service.submit(()->{
            log.debug("begin...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            countDownLatch.countDown();
            log.debug("end...{}", countDownLatch.getCount());
        });

        service.submit(()->{
            log.debug("begin...");
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            countDownLatch.countDown();
            log.debug("end...{}", countDownLatch.getCount());
        });

        service.submit(()->{
            log.debug("begin...");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            countDownLatch.countDown();
            log.debug("end...{}", countDownLatch.getCount());
        });
        service.submit(()->{
            log.debug("waiting...");
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.debug("waiting end...");
        });
    }

    private static void test4() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(3);
        new Thread(()->{
            log.debug("begin...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            countDownLatch.countDown();
            log.debug("end...");
        }).start();
        new Thread(()->{
            log.debug("begin...");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            countDownLatch.countDown();
            log.debug("end...");
        }).start();
        new Thread(()->{
            log.debug("begin...");
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            countDownLatch.countDown();
            log.debug("end...");
        }).start();

        log.debug("waiting...");
        countDownLatch.await();
        log.debug("waiting end...");
    }
}
