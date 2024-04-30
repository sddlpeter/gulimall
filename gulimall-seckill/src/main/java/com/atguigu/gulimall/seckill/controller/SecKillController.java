package com.atguigu.gulimall.seckill.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.service.SecKillService;
import com.atguigu.gulimall.seckill.to.SecKillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SecKillController {


    @Autowired
    SecKillService secKillService;

    @GetMapping("/currentSecKillSkus")
    public R getCurrentSecKillSkus() {

        List<SecKillSkuRedisTo> vos = secKillService.getCurrentSecKillSkus();
        return R.ok().setData(vos);
    }

    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSecKillInfo(@PathVariable("skuId") Long skuId) {
        SecKillSkuRedisTo to = secKillService.getSkuSecKillInfo(skuId);
        return R.ok().setData(to);
    }
}
