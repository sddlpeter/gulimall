package com.atguigu.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class GuliFeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 1. RequestContextHolder 拿到刚进来的请求
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                if (attributes != null) {
                    System.out.println("interceptor 线程..." + Thread.currentThread().getId());

                    HttpServletRequest request = attributes.getRequest(); // 老请求

                    if (request != null) {
                        // 2. 同步请求头信息, 主要是同步cookie
                        String cookie = request.getHeader("Cookie");
                        // 3. 给新请求同步了老请求的cookie
                        template.header("Cookie", cookie);
                    }
                }
            }
        };
    }
}
