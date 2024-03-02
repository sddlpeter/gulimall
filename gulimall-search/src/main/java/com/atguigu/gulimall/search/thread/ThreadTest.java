package com.atguigu.gulimall.search.thread;

import java.util.concurrent.*;

public class ThreadTest {

    // 1. 创建线程池
    public static ExecutorService executor = Executors.newFixedThreadPool(10);


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main....start....");
//        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 5;
//            System.out.println("运行结果：i=" + i);
//        }, executor);

//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 0;
//            System.out.println("运行结果：i=" + i);
//            return i;
//        }, executor).whenComplete((res,exception) -> {
//            System.out.println("异步任务完成了...结果是：" + res + "， 异常是： " + exception);
//
//        }).exceptionally(throwable -> {
//            return 10;
//        });


//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("运行结果：i=" + i);
//            return i;
//        }, executor).handle((res, thr) -> {
//            if (res != null) {
//                return res * 2;
//            }
//            if (thr != null) {
//                return 0;
//            }
//            return 0;
//        });


//        CompletableFuture<Object> future01 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务1线程：" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("任务1结果：i=" + i);
//            return i;
//        }, executor);
//
//        CompletableFuture<Object> future02 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务2线程：" + Thread.currentThread().getId());
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            System.out.println("任务2结束");
//            return "hello";
//        }, executor);


        // 两个方法都执行完成，才执行任务3
//        future01.runAfterBothAsync(future02,()-> {
//            System.out.println("任务3开始....");
//        } ,executor);

//        future01.thenAcceptBothAsync(future02, (f1, f2) -> {
//            System.out.println("任务3开始...之前的结果：" + f1 + "----" + f2);
//        }, executor);

//        CompletableFuture<String> future = future01.thenCombineAsync(future02, (f1, f2) -> {
//            return f1 + " + " + f2 + " -> haha";
//        }, executor);


        // 两个方法只需一个执行完成，就执行任务3
//        future01.runAfterEitherAsync(future02, () -> {
//            System.out.println("任务3开始....");
//        },executor);

//        future01.acceptEitherAsync(future02, (res) -> {
//            System.out.println("任务3开始...." + res);
//        }, executor);

//        CompletableFuture<String> future = future01.applyToEitherAsync(future02, (res) -> {
//            System.out.println("任务3开始....");
//            return res.toString() + "-> 哈哈";
//        }, executor);

        CompletableFuture<String> futureImg = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品图片信息");
            return "hello.jpg";
        },executor);

        CompletableFuture<String> futureAttr = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品属性信息");
            return "黑色+256G";
        }, executor);


        CompletableFuture<String> futureDesc = CompletableFuture.supplyAsync(() -> {

            try {
                Thread.sleep(3000);
                System.out.println("查询商品介绍");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "华为";
        }, executor);


        // 由于是由当前线程执行，那么如果每个方法都执行的慢，会有阻塞式等待
//        futureImg.get(); // 1s
//        futureAttr.get(); // 2s
//        futureDesc.get(); // 1.5s

        // CompletableFuture<Void> allOf = CompletableFuture.allOf(futureImg, futureAttr, futureDesc);
        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(futureImg, futureAttr, futureDesc);
        anyOf.join();  // 等待所有结果完成


        // System.out.println("main....end " + futureImg.get() + " " + futureAttr.get() + " " + futureDesc.get());

        System.out.println("main....end " + anyOf.get());
    }



    public void thread(String[] args) {
        System.out.println("main....start....");

        //2. 原生方法创建线程池
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5,
                200,
                10,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(100000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
        executor.execute(new Runnable01());
        System.out.println("main....end....");
    }

    public static class Runnable01 implements Runnable {

        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 5;
            System.out.println("运行结果：i=" + i);
        }
    }
}
