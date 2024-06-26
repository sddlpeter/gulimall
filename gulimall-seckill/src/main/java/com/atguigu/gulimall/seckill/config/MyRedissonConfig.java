package com.atguigu.gulimall.seckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class MyRedissonConfig {

    // 所有对 redisson的操作，都要通过redisson client
    @Bean(destroyMethod="shutdown")
    RedissonClient redisson() throws IOException {
        //1. 创建配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.56.10:6379");

        //2. 根据config创建出RedissonClient 实例
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
