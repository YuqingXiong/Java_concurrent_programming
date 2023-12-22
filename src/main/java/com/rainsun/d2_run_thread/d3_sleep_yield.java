package com.rainsun.d2_run_thread;

import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = "c.d3_sleep_yield")
public class d3_sleep_yield {
    public static void main(String[] args) {
        Thread t1 = new Thread("t1"){
            @Override
            public void run() {
                log.debug("enter sleep");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    log.debug("wake up ...");
                   e.printStackTrace();
                }
            }
        };
        t1.start();

        try {
            Thread.sleep(1000); // 当前 main 线程睡眠1s
            log.debug("interrupt...");
            t1.interrupt(); // 打断 t1 的 sleep

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /**
         * 输出：
         * 14:45:29 [t1] c.d3_sleep_yield - enter sleep
         * 14:45:30 [main] c.d3_sleep_yield - interrupt...
         * 14:45:30 [t1] c.d3_sleep_yield - wake up ...
         * java.lang.InterruptedException: sleep interrupted
         */
    }
}
