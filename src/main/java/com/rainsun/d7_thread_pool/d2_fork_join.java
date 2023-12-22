package com.rainsun.d7_thread_pool;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

@Slf4j(topic = "c.d2_fork_join")
public class d2_fork_join {
    public static void main(String[] args) {
        ForkJoinPool pool = new ForkJoinPool(4);
        System.out.println(pool.invoke(new AddTask(0, 5)));
    }
}

class AddTask extends RecursiveTask<Integer>{
    private int begin;
    private int end;

    public AddTask(int begin, int end) {
        this.begin = begin;
        this.end = end;
    }

    @Override
    protected Integer compute() {
        if(begin == end) return begin;
        if(end - begin == 1) return end + begin;

        int mid = (end+begin) / 2;
        AddTask t1 = new AddTask(begin, mid);
        AddTask t2 = new AddTask(mid + 1, end);
        t1.fork();
        t2.fork();
        return t1.join() + t2.join();

    }
}

class MyTask extends RecursiveTask<Integer>{
    private int n;

    public MyTask(int n) {
        this.n = n;
    }

    @Override
    protected Integer compute() {
        if(n == 1) return 1;

        MyTask t1 = new MyTask(n-1);
        t1.fork(); // fork 让 pool 中的一个线程执行 t1 任务，也就是执行t1中的compute方法

        return n + t1.join();
    }
}
