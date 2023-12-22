package com.rainsun.d6_atomic;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class d4_AtomicField {
    public static void main(String[] args) {
        Student student = new Student();
        AtomicReferenceFieldUpdater updater = AtomicReferenceFieldUpdater.newUpdater(Student.class, String.class, "name");

        boolean flag = updater.compareAndSet(student, null, "rainsun");
        System.out.println(flag);
        System.out.println(student);
    }
}

class Student{
    volatile String name;

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                '}';
    }
}
