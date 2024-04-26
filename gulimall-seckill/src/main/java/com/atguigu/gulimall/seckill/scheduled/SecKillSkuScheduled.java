package com.atguigu.gulimall.seckill.scheduled;


import com.atguigu.gulimall.seckill.service.SecKillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SecKillSkuScheduled {

    @Autowired
    SecKillService secKillService;

    @Autowired
    RedissonClient redissonClient;

    private final String upload_lock = "seckill:upload:lock";


    // TODO 上架应该设置为幂等性，上架了的商品就不能再上架了
    @Scheduled(cron = "0 * * * * ?")
    public void uploadSecKillSkuLatest3Days() {
        log.info("上架秒杀信息....");

        // 分布式锁
        RLock lock = redissonClient.getLock(upload_lock);
        lock.lock(10, TimeUnit.SECONDS);

        try {
            secKillService.uploadSecKillSkuLatest3Days();
        } finally {
            lock.unlock();
        }

    }
}
