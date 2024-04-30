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

import java.util.*;
import java.util.regex.Pattern;
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

    @Override
    public List<SecKillSkuRedisTo> getCurrentSecKillSkus() {
        // 1. 确定当前时间属于哪个秒杀场次
        long time = new Date().getTime();
        Set<String> keys = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
        for (String key : keys) {
            String replace = key.replace(SESSIONS_CACHE_PREFIX, "");
            String[] s = replace.split("_");
            long start = Long.parseLong(s[0]);
            long end = Long.parseLong(s[1]);
            if (time >= start && time <= end) {
                // 2. 获取这个秒杀场次需要的所有商品信息
                List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                List<String> list = hashOps.multiGet(range);
                if (list != null) {
                    List<SecKillSkuRedisTo> collect = list.stream().map(item -> {
                        SecKillSkuRedisTo redis = JSON.parseObject((String) item, SecKillSkuRedisTo.class);
                        // redis.setRandomCode(null); 当前秒杀开始了，需要随机码
                        return redis;
                    }).collect(Collectors.toList());
                    return collect;
                }
                break;
            }
        }
        return null;
    }

    @Override
    public SecKillSkuRedisTo getSkuSecKillInfo(Long skuId) {
        //1. 找到所有需要参与秒杀的商品的key
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();
        if(keys != null && keys.size() > 0) {
            String regx = "\\d_" + skuId;
            for (String key : keys) {
                if (Pattern.matches(regx, key)) {
                    String json = hashOps.get(key);
                    SecKillSkuRedisTo redisTo = JSON.parseObject(json, SecKillSkuRedisTo.class);

                    // 随机码
                    Long startTime = redisTo.getStartTime();
                    Long endTime = redisTo.getEndTime();
                    Long current = new Date().getTime();
                    if(current >= startTime && current <= endTime) {
                    } else {
                        redisTo.setRandomCode(null);
                    }
                    return redisTo;
                }
            }
        }
        return null;
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
