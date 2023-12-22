package com.rainsun.d3_synchronized;

import java.util.Hashtable;

public class d3_ThreadSafeClass {
    public static void main(String[] args) {
        Hashtable<String, String> table = new Hashtable();
        // 线程1，线程2
        String value = "v1";
        if( table.get("key") == null) {
            table.put("key", value);
        }
    }
}
