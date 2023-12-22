package com.rainsun.d3_synchronized;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.d6_GuardedObject")
public class d6_GuardedObject {
    private Object response;
    private final Object lock = new Object();
    public Object get() {
        synchronized (lock) {
            // 条件不满足则等待
            while (response == null) {
                log.debug("等待 response ");
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return response;
        }
    }
    public void complete(Object response) {
        synchronized (lock) {
            // 条件满足，通知等待线程
            this.response = response;
            lock.notifyAll();
        }
    }

    public static void main(String[] args) {
        d6_GuardedObject guardedObject = new d6_GuardedObject();
        new Thread(()->{
            Object myResponse = guardedObject.get();
            log.debug("myResponse: {}", myResponse);
        }, "t1").start();

        new Thread(()->{
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            guardedObject.complete("等待结束...");
        }, "t2").start();
    }
}
