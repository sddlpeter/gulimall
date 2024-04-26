package com.atguigu.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.service.SecKillService;
import com.atguigu.gulimall.seckill.to.SecKillSkuRedisTo;
import com.atguigu.gulimall.seckill.vo.SecKillSessionsWithSkus;
import com.atguigu.gulimall.seckill.vo.SecKillSkuVo;
import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SecKillServiceImpl implements SecKillService {
    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RedissonClient redissonClient;



    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";

    private final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";

    private final String SKUKILL_CACHE_PREFIX = "seckill:skus:";

    @Override
    public void uploadSecKillSkuLatest3Days() {
        // 1. 扫描最近三天需要参与的秒杀活动
        R session = couponFeignService.getLatest3DaySession();
        if (session.getCode() == 0) {
            // 上架
            List<SecKillSessionsWithSkus> sessionData = session.getData(new TypeReference<List<SecKillSessionsWithSkus>>() {
            });

            // 缓存到redis
            // 1. 缓存活动信息
            saveSessionInfo(sessionData);
            // 2. 缓存活动关联的商品信息
            saveSessionSkuInfo(sessionData);
        }
    }

    private void saveSessionInfo(List<SecKillSessionsWithSkus> sessions) {
        sessions.stream().forEach(session -> {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();

            String key = SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;

            // 缓存活动信息
            Boolean hasKey = redisTemplate.hasKey(key);
            if (!hasKey) {
                List<String> collect = session.getRelationSkus().stream().map(item ->
                        item.getPromotionSessionId() + "_" + item.getSkuId().toString()).collect(Collectors.toList());
                redisTemplate.opsForList().leftPushAll(key, collect);
            }
        });
    }

    private void saveSessionSkuInfo(List<SecKillSessionsWithSkus> sessions) {
        sessions.stream().forEach(session -> {
            // 准备hash操作
            BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            session.getRelationSkus().stream().forEach(secKillSkuVo -> {
                // 4. 随机串  seckill?skuId=1&key=fdui328jik
                String token = UUID.randomUUID().toString().replace("-", "");

                if (!ops.hasKey(secKillSkuVo.getPromotionSessionId().toString() + "_" + secKillSkuVo.getSkuId().toString())) {
                    // 缓存商品
                    SecKillSkuRedisTo redisTo = new SecKillSkuRedisTo();
                    // 1. sku的基本数据
                    R skuInfo = productFeignService.getSkuInfo(secKillSkuVo.getSkuId());
                    if (skuInfo.getCode() == 0) {
                        SkuInfoVo info = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        redisTo.setSkuInfoVo(info);
                    }

                    // 2. sku的秒杀信息
                    BeanUtils.copyProperties(secKillSkuVo, redisTo);

                    // 3. 设置商品秒杀时间信息
                    redisTo.setStartTime(session.getStartTime().getTime());
                    redisTo.setEndTime(session.getEndTime().getTime());


                    redisTo.setRandomCode(token);

                    String jsonString = JSON.toJSONString(redisTo);
                    ops.put(secKillSkuVo.getPromotionSessionId().toString() + "_" +  secKillSkuVo.getSkuId().toString(), jsonString);

                    // 5. 设置信号量，限流
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    semaphore.trySetPermits(secKillSkuVo.getSeckillCount().intValue());
                }

            });
        });
    }
}
