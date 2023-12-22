package com.rainsun.d2_run_thread;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.d2_run_start")
public class d2_run_start {
    public static void main(String[] args) {
        Thread t1 = new Thread("t1") {
            @Override
            public void run() {
                log.debug("running");
            }
        };

        t1.run();
        // 14:25:50 [main] c.d2_run_start - running
        System.out.println(t1.getState()); //NEW

        t1.start();
        // 14:28:11 [t1] c.d2_run_start - running
        System.out.println(t1.getState()); // RUNNABLE
    }
}
