package com.atguigu.gulimall.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
//@EnableAsync
//@EnableScheduling
public class HelloSchedule {


    // 1. 定时任务默认是阻塞的，可以使用异步编排，每次都从线程池里拿到线程执行任务
    //   ComputableFuture.runAsync(() -> {
    //       xxxService.hello();
    //   }, executor);

    // 2. 使用支持定时任务的的线程池
    // spring.task.scheduling.pool.size=5

    // 3. 让定时任务异步执行




    @Async
    @Scheduled(cron = "* * 1 * * ?")
    public void hello() throws InterruptedException {
        log.info("hello...." + new Date());
    }
}
