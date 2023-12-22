package com.rainsun.d2_run_thread;

public class d1_Frame {
    public static void main(String[] args) {

        Thread t1 = new Thread() {
            @Override
            public void run() {
                method1(20); // t1 线程调用 method1
            }
        };
        t1.setName("t1");
        t1.start();
        method1(10); // main 调用method1
    }

    public static void method1(int x){
        int y = x + 1;
        Object obj = method2(); // 调用 method2
    }
    public static Object method2(){
        Object obj = new Object();
        return obj;
    }
}
