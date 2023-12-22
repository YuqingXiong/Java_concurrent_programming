package com.rainsun.d2_run_thread;

import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = "c.d5_join")
public class d5_join {
//    static int r = 0;
//    public static void main(String[] args) throws InterruptedException {
//        test1();
//    }

    static int r1 = 0;
    static int r2 = 0;
    public static void main(String[] args) throws InterruptedException {
        test2();
    }
    private static void test2() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            r1 = 10;
        });
        Thread t2 = new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            r2 = 20;
        });
        long start = System.currentTimeMillis();
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        long end = System.currentTimeMillis();
        log.debug("r1: {} r2: {} cost: {}", r1, r2, end - start);
        // 15:23:57 [main] c.d5_join - r1: 10 r2: 20 cost: 2014
    }
//    private static void test1() throws InterruptedException {
//        log.debug("开始");
//        Thread t1 = new Thread(() -> {
//            log.debug("开始");
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            log.debug("结束");
//            r = 10;
//        });
//        t1.start();
//        t1.join(); // 等待 t1 完成，再获取 r的结果
//        log.debug("结果为:{}", r);
//        log.debug("结束");
//    }
}
