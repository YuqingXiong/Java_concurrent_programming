package com.rainsun.d7_thread_pool;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j(topic = "c.d1_mypool")
public class d1_mypool {
    public static void main(String[] args) {
        ThreadPool threadPool = new ThreadPool(
                1,
                1000, TimeUnit.MILLISECONDS,
                1,
                (queue, task) -> {
                    // 1. 一直等待
//                    queue.put(task);
                    // 2. 带超时等待
//                    queue.offer(task, 500, TimeUnit.MILLISECONDS);
                    // 3. 放弃执行
//                    log.debug("放弃 {}", task);
                    // 4. 抛出异常,与放弃执行的区别是后面的任务不会被执行
//                    throw new RuntimeException("任务执行失败" + task);
                    // 5. 调用这自己执行任务
                    task.run();
                });

        for(int i = 0; i < 3; ++ i){
            int j = i;
            threadPool.execute(()->{
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.debug("{}", j);
            });
        }
    }
}

// 拒绝策略
@FunctionalInterface
interface RejectPolicy<T>{
    void reject(BlockQueue<T> queue, T task);
}

@Slf4j(topic = "c.ThreadPool")
class ThreadPool{
    // 任务队列
    private BlockQueue<Runnable> taskQueue;

    // 线程集合
    private HashSet<Worker> workers = new HashSet<>();

    // 核心线程数
    private int coreSize;

    // 获取任务的超时时间
    private long timeout;
    private TimeUnit timeUnit;

    // 拒绝策略
    private RejectPolicy<Runnable> rejectPolicy;

    public ThreadPool(int coreSize, long timeout, TimeUnit timeUnit, int queueCapacity, RejectPolicy<Runnable> rejectPolicy) {
        this.coreSize = coreSize;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.taskQueue = new BlockQueue<>(queueCapacity);
        this.rejectPolicy = rejectPolicy;
    }

    // 执行任务
    public void execute(Runnable task){
        // 任务数没有超过 coreSize 时，直接交给 worker 对象执行
        // 如果任务数超过 coreSize 时，加入任务队列 taskQueue 暂存
        synchronized (workers){
            if(workers.size() < coreSize){
                Worker worker = new Worker(task);
                log.debug("新增 worker{}, task{}", worker, task);
                workers.add(worker);
                worker.start();
            }else{
//                taskQueue.put(task);

                /**
                 * 拒绝策略：
                 * 1. 一直等待
                 * 2. 带超时的等待
                 * 3. 放弃任务执行
                 * 4. 抛出异常
                 * 5. 调用者自己执行任务
                 */
                taskQueue.tryPut(rejectPolicy, task);
            }
        }
    }

    class Worker extends Thread{
        private Runnable task;
        public Worker(Runnable task){
            this.task = task;
        }

        @Override
        public void run(){
            // 执行任务
            // 1. task不为空，执行任务
            // 2. task 为空，则接着从任务队列获取新任务再执行任务
            while(task != null || (task = taskQueue.poll(timeout, timeUnit)) != null){
                try {
                    log.debug("正在执行任务...{}", task);
                    task.run();
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    task = null;
                }
            }
            synchronized(workers){
                log.debug("worker 被移除 {}", this);
                workers.remove(this);
            }
        }
    }
}

@Slf4j(topic = "c.BlockQueue")
class BlockQueue<T>{
    // 1. 任务队列
    private Deque<T> queue = new ArrayDeque<>();

    // 2. 锁
    private ReentrantLock lock = new ReentrantLock();

    // 3. 生产者条件变量
    private Condition fullWaitSet = lock.newCondition();

    // 4. 消费者条件变量，获取 task 任务
    private Condition emptyWaitSet = lock.newCondition();

    // 5. 容量
    private int capacity;

    public BlockQueue(int capacity) {
        this.capacity = capacity;
    }

    // 带超时的阻塞获取
    public T poll(long timeout, TimeUnit unit){
        lock.lock();
        try {
            // 将 timeout 时间统一转换为 纳秒
            long nanos = unit.toNanos(timeout);
            while(queue.isEmpty()){
                try{
                    if(nanos <= 0){
                        return null;
                    }
                    // 返回剩余等待时间
                    nanos = emptyWaitSet.awaitNanos(nanos);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            T t = queue.removeFirst();
            fullWaitSet.signal();
            return t;
        } finally {
            lock.unlock();
        }
    }

    // 获取任务
    public T take(){
        lock.lock();
        try {
            while(queue.isEmpty()){
                try{
                    emptyWaitSet.await();
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            T t = queue.removeFirst();
            fullWaitSet.signal();
            return t;
        } finally {
            lock.unlock();
        }
    }

    // 添加任务
    public void put(T task){
        lock.lock();
        try {
            while(queue.size() == capacity){
                try{
                    log.debug("等待加入任务队列 {}", task);
                    fullWaitSet.await();
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.debug("加入任务队列 {}", task);
            queue.addLast(task);
            emptyWaitSet.signal();
        }finally {
            lock.unlock();
        }
    }

    // 具有超时时间的阻塞添加
    public boolean offer(T task, long timeout, TimeUnit timeUnit){
        lock.lock();
        try {
            long nanos = timeUnit.toNanos(timeout);
            while(queue.size() == capacity){
                try{
                    if(nanos <= 0){
                        return false;
                    }
                    log.debug("等待加入任务队列 {}", task);
                    nanos = fullWaitSet.awaitNanos(nanos);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.debug("加入任务队列 {}", task);
            queue.addLast(task);
            emptyWaitSet.signal();
            return true;
        }finally {
            lock.unlock();
        }
    }

    public int getSize(){
        lock.lock();;
        try {
            return queue.size();
        }finally {
            lock.unlock();
        }
    }

    public void tryPut(RejectPolicy<T> rejectPolicy, T task){
        lock.lock();;
        try {
            if(queue.size() == capacity){
                rejectPolicy.reject(this, task);
            }else{
                log.debug("加入任务队列 {}", task);
                queue.addLast(task);
                emptyWaitSet.signal();
            }
        }finally {
            lock.unlock();
        }
    }
}
