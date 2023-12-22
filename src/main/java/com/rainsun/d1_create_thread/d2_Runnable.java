package com.rainsun.d1_create_thread;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.Test2")
public class d2_Runnable {
    public static void main(String[] args) {
//        Runnable r = new Runnable() {
//            @Override
//            public void run() {
//                log.debug("running");
//            }
//        };
        // lambda精简代码
        Runnable r = ()->log.debug("running");
        Thread t = new Thread(r, "t2");
        t.start();
    }
}
