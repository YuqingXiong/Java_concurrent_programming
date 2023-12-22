package com.rainsun.d4_memory;

public final class d1_Singleton {
    private d1_Singleton() { }
    private static d1_Singleton INSTANCE = null;
    public static d1_Singleton getInstance() {
        if(INSTANCE == null) { // t2
            // 首次访问会同步，而之后的使用没有 synchronized
            synchronized(d1_Singleton.class) {
                if (INSTANCE == null) { // t1
                    INSTANCE = new d1_Singleton();
                }
            }
        }
        return INSTANCE;
    }
}