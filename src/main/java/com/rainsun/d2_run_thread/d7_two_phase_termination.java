package com.rainsun.d2_run_thread;

import lombok.extern.slf4j.Slf4j;

class Test{
    public static void main(String[] args) throws InterruptedException {
        d7_two_phase_termination tpt = new d7_two_phase_termination();
        tpt.start();

        Thread.sleep(3500);
        tpt.stop();
        /**
         * 16:07:57 [Thread-0] c.d7_two_phase_termination - 执行监控记录
         * 16:07:58 [Thread-0] c.d7_two_phase_termination - 执行监控记录
         * 16:07:59 [Thread-0] c.d7_two_phase_termination - 执行监控记录
         * java.lang.InterruptedException: sleep interrupted
         * 	at java.base/java.lang.Thread.sleep0(Native Method)
         * 	at java.base/java.lang.Thread.sleep(Thread.java:509)
         * 	at com.rainsun.d2_run_thread.d7_two_phase_termination.lambda$start$0(d7_two_phase_termination.java:30)
         * 	at java.base/java.lang.Thread.run(Thread.java:1583)
         * 16:08:00 [Thread-0] c.d7_two_phase_termination - 释放资源，锁，料理后事...
         */
    }
}

@Slf4j(topic = "c.d7_two_phase_termination")
public class d7_two_phase_termination {
    private Thread monitor;

    // 启动监控程序
    public void start(){
        monitor = new Thread(()->{
            while (true){
                Thread currentThread = Thread.currentThread();
                if(currentThread.isInterrupted()){
                    log.debug("释放资源，锁，料理后事...");
                    break;
                }

                try {
                    Thread.sleep(1000); // 情况一：sleep过程被打断
                    log.debug("执行监控记录"); // 情况二：执行其他过程被打断
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    // sleep过程中被打断会重置打断标记为false
                    // 这里需要重新设置打断标记为 true，以便在上面的if里判断标记，进行后续处理
                    currentThread.interrupt();
                }
            }
        });
        monitor.start();
    }

    public void stop(){
        monitor.interrupt();
    }
}
