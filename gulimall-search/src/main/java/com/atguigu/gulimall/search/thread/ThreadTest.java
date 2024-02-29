package com.atguigu.gulimall.search.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadTest {
    public static ExecutorService service = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        System.out.println("main....start....");
        service.execute(new Runnable01());
        System.out.println("main....end....");
    }

    public static class Runnable01 implements Runnable {

        @Override
        public void run() {
            int i = 10/5;
            System.out.println("i=" + i);
        }
    }
}
