package com.rainsun.d8_JUC;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j(topic = "c.d2_ReadWriteLockTest")
public class d2_ReadWriteLockTest {
    public static void main(String[] args) throws InterruptedException {
        DataContainer dataContainer = new DataContainer();
        new Thread(()->{
            dataContainer.read();
        },"t1").start();
        Thread.sleep(100);
        new Thread(()->{
            dataContainer.write();
        },"t2").start();
    }
}

@Slf4j(topic = "c.DataContainer")
class DataContainer{
    private Object data;

    private ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock.ReadLock r = rw.readLock();
    private ReentrantReadWriteLock.WriteLock w = rw.writeLock();

    public Object read(){
        log.debug("get read lock ...");
        r.lock();
        try {
            log.debug("read");
            Thread.sleep(1000);
            return data;
        } catch (InterruptedException e)  {
            throw new RuntimeException(e);
        } finally {
            r.unlock();
            log.debug("release read lock...");
        }
    }

    public void write(){
        log.debug("get write lock");
        w.lock();
        try {
            log.debug("write");
        }finally {
            log.debug("release write lock");
            w.lock();
        }

    }
}