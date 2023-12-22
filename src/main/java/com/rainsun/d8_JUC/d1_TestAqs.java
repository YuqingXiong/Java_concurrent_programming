package com.rainsun.d8_JUC;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

@Slf4j(topic = "c.d1_TestAqs")
public class d1_TestAqs {
    public static void main(String[] args) {
        MyLock lock = new MyLock();
        new Thread(()->{
            lock.lock();
            try {
                log.debug("locking...");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                log.debug("unlocking...");
                lock.unlock();
            }
        }, "t1").start();

        new Thread(()->{
            lock.lock();
            try {
                log.debug("locking...");
            } finally {
                log.debug("unlocking...");
                lock.unlock();
            }
        }, "t2").start();
    }
}

// 自定义锁（不可重入锁）
class MyLock implements Lock {
    private MySync sync = new MySync();

    class MySync extends AbstractQueuedSynchronizer{
        @Override // 独占锁
        protected boolean tryAcquire(int arg) {
            if(compareAndSetState(0, 1)){
                // 加上了锁，设置 owner 为当前线程
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        @Override
        protected boolean tryRelease(int arg) {
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        @Override // 是否持有独占锁
        protected boolean isHeldExclusively() {
            return getState() == 1;
        }

        public Condition newCondition(){
            return new ConditionObject();
        }
    }

    @Override
    public void lock() {
        sync.acquire(1);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }

    @Override
    public boolean tryLock() {
        return sync.tryAcquire(1);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(time));
    }

    @Override
    public void unlock() {
        sync.release(1);
    }

    @Override
    public Condition newCondition() {
        return sync.newCondition();
    }
}