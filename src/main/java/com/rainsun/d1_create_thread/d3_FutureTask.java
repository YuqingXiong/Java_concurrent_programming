package com.rainsun.d1_create_thread;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;


@Slf4j(topic = "c.Test3")
public class d3_FutureTask {
    public static void main(String[] args) throws Exception {
//        FutureTask<Integer> task =  new FutureTask<>(new Callable<Integer>() {
//            @Override
//            public Integer call() throws Exception {
//                log.debug("running");
//                Thread.sleep(1000);
//                return 100;
//            }
//        });

        // 简化：
        FutureTask<Integer> task = new FutureTask<>(()->{
            log.debug("running");
            Thread.sleep(1000);
            return 100;
        });

        new Thread(task, "t3").start();
        Integer result = task.get();
        log.debug("result = {}", result);
    }
}
