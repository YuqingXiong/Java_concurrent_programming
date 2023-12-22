package com.rainsun.d2_run_thread;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.d8_daemon")
public class d8_daemon {
    public static void main(String[] args) throws InterruptedException {
        log.debug("开始运行...");
        Thread t1 = new Thread(() -> {
            log.debug("开始运行...");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.debug("运行结束...");
        }, "daemon");
        // 设置该线程为守护线程
        t1.setDaemon(true);
        t1.start();
        Thread.sleep(1000);
        log.debug("运行结束...");
        /**
         * 16:22:39 [main] c.d8_daemon - 开始运行...
         * 16:22:39 [daemon] c.d8_daemon - 开始运行...
         * 16:22:40 [main] c.d8_daemon - 运行结束...
         */
    }
}
