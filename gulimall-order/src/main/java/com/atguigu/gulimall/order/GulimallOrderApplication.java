package com.atguigu.gulimall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableAspectJAutoProxy(exposeProxy = true)  // aspectj 动态代理，(默认使用jdk动态代理), 用代理对象互相调用，就会有事务产生了
@EnableFeignClients
@EnableRedisHttpSession
@EnableRabbit
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallOrderApplication {
	public static void main(String[] args) {
		SpringApplication.run(GulimallOrderApplication.class, args);
	}
}
