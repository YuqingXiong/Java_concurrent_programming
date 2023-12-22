package com.rainsun.d2_run_thread;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.d9_thread_state")
public class d9_thread_state {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(()->{
            log.debug("running..."); // NEW
        }, "t1");


        Thread t2 = new Thread(()->{
            while (true){ // RUNNABLE

            }
        }, "t2");
        t2.start();

        Thread t3 = new Thread(()->{
            log.debug("running..."); // TERMINATED
        }, "t3");
        t3.start();

        Thread t4 = new Thread(()->{
            synchronized (d9_thread_state.class){
                try {
                    Thread.sleep(1000000); // timed_waiting
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }, "t4");
        t4.start();

        Thread t5 = new Thread(()->{
            try {
                t2.join(); // t5等待t2完成，t2为死循环，所以是 waiting状态
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "t5");
        t5.start();

        Thread t6 = new Thread(()->{
            // 这个锁已经被t4占据了，所以t6拿不到这个锁。陷入 blocked状态
            synchronized (d9_thread_state.class){
                try {
                    Thread.sleep(1000000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }, "t6");
        t6.start();

        Thread.sleep(500);

        log.debug("t1 state {}", t1.getState());
        log.debug("t2 state {}", t2.getState());
        log.debug("t3 state {}", t3.getState());
        log.debug("t4 state {}", t4.getState());
        log.debug("t5 state {}", t5.getState());
        log.debug("t6 state {}", t6.getState());
        /** 输出：
         * 16:47:45 [t3] c.d9_thread_state - running...
         * 16:47:46 [main] c.d9_thread_state - t1 state NEW
         * 16:47:46 [main] c.d9_thread_state - t2 state RUNNABLE
         * 16:47:46 [main] c.d9_thread_state - t3 state TERMINATED
         * 16:47:46 [main] c.d9_thread_state - t4 state TIMED_WAITING
         * 16:47:46 [main] c.d9_thread_state - t5 state WAITING
         * 16:47:46 [main] c.d9_thread_state - t6 state BLOCKED
         */

    }
}
