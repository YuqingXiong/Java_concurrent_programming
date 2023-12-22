package com.rainsun.d6_atomic;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

public class d1_AtomicInterger {
    public static void main(String[] args) {
        AtomicInteger i = new AtomicInteger(0);

        /**
         * get 在前获取的计算前的结果
         * get 在后获取的计算后的结果
         */

        // ++i
        System.out.println(i.incrementAndGet()); // 1

        // i++
        System.out.println(i.getAndIncrement()); // 1

        // 2 + 5 = 7
        System.out.println(i.getAndAdd(5)); // 2

        // 7 + 5 = 12
        System.out.println(i.addAndGet(5)); // 12

        i.updateAndGet(operand -> operand * 10);
        System.out.println(i.get());


    }

}


