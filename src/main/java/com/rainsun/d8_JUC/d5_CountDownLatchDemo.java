package com.rainsun.d8_JUC;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class d5_CountDownLatchDemo {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);
        String[] allstate = new String[10];
        for(int i = 0; i < 10;  ++ i){
            int finalI = i;
            new Thread(()->{
                for(int load = 0; load <= 100; ++ load){
                    allstate[finalI] = load + "%";
                    System.out.print("\r" + Arrays.toString(allstate));
                }
                latch.countDown();
            }).start();
        }
        latch.await();
        System.out.println("\nGame begin !");
        service.shutdown();
    }
}
