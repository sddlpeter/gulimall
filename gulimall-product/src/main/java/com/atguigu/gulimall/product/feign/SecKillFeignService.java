package com.atguigu.gulimall.product.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-seckill")
public interface SecKillFeignService {

    @GetMapping("/sku/seckill/{skuId}")
    R getSkuSecKillInfo(@PathVariable("skuId") Long skuId);
}
