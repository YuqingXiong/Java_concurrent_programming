package com.rainsun.d3_synchronized;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.d1_problem")
public class d1_problem {
    static int counter = 0;
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 5000; i++) {
                counter++;
            }
        }, "t1");
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 5000; i++) {
                counter--;
            }
        }, "t2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        log.debug("{}",counter);
        // 不一定为 0
        // 输出 102 ：21:04:59 [main] c.d1_problem - 102
    }
}
