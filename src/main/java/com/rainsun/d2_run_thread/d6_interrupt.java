package com.rainsun.d2_run_thread;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.d6_interrupt")
public class d6_interrupt {

    public static void main(String[] args) throws InterruptedException {
        test2();
    }

    private static void test2() throws InterruptedException {
        Thread t2 = new Thread(()->{
            while(true) {
                // 获取当前线程的打断标记
                Thread current = Thread.currentThread();
                boolean interrupted = current.isInterrupted();
                // 自己决定是否退出当前线程
                if(interrupted) {
                    log.debug(" 打断状态: {}", interrupted);
                    // 15:39:55 [t2] c.d6_interrupt -  打断状态: true
                    break;
                }
            }
        }, "t2");
        t2.start();
        Thread.sleep(500);
        t2.interrupt();
    }

    private static void test1() throws InterruptedException {
        Thread t1 = new Thread(()->{
            log.debug("sleep...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "t1");
        t1.start();
        Thread.sleep(500);
        log.debug("interrupt...");
        t1.interrupt();
        log.debug(" 打断状态: {}", t1.isInterrupted());
        /** 输出：
         * 15:36:35 [t1] c.d6_interrupt - sleep...
         * 15:36:36 [main] c.d6_interrupt - interrupt...
         * 15:36:36 [main] c.d6_interrupt -  打断状态: false
         */
    }
}
