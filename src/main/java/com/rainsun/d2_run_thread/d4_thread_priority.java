package com.rainsun.d2_run_thread;

public class d4_thread_priority {
    public static void main(String[] args) {
        Thread t1 = new Thread(()->{
            int count = 0;
            while(true){
                System.out.println("---> 1 " + count++);
            }
        }, "t1");


        Thread t2 = new Thread(()->{
            int count = 0;
            while(true){
//                Thread.yield();
                System.out.println("         ---> 2 " + count++);
            }
        }, "t2");

        // 设置线程优先级（默认优先级为5）
        t1.setPriority(Thread.MIN_PRIORITY);
        t2.setPriority(Thread.MAX_PRIORITY);
        t1.start();
        t2.start();
    }
}
