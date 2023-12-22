package com.rainsun.d6_atomic;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicStampedReference;

@Slf4j(topic = "c.d2_ABA")
public class d2_ABA {
    static AtomicStampedReference<String> ref = new AtomicStampedReference<>("A", 0);

    public static void main(String[] args) throws InterruptedException {
        log.debug("main start...");

        String prev = ref.getReference();
        int stamp = ref.getStamp(); // 获取版本号
        log.debug("stamp: {}", stamp);

        other();
        Thread.sleep(1000);
        log.debug("stamp: {}", stamp);
        log.debug("change A->C {}", ref.compareAndSet(prev, "C", stamp, stamp+1));
    }

    private static void other() throws InterruptedException {
        new Thread(()->{
            int stamp = ref.getStamp();
            log.debug("stamp: {}", stamp);
            log.debug("change A->B {}", ref.compareAndSet(ref.getReference(), "B", stamp, stamp + 1));
        }, "t1").start();
        Thread.sleep(500);
        new Thread(()->{
            int stamp = ref.getStamp();
            log.debug("stamp: {}", stamp);
            log.debug("change B->A {}", ref.compareAndSet(ref.getReference(), "A", stamp, stamp + 1));
        }, "t2").start();
    }
}
