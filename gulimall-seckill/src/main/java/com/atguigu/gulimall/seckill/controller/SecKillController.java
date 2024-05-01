package com.atguigu.gulimall.seckill.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.service.SecKillService;
import com.atguigu.gulimall.seckill.to.SecKillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class SecKillController {


    @Autowired
    SecKillService secKillService;

    @ResponseBody
    @GetMapping("/currentSecKillSkus")
    public R getCurrentSecKillSkus() {

        List<SecKillSkuRedisTo> vos = secKillService.getCurrentSecKillSkus();
        return R.ok().setData(vos);
    }

    @ResponseBody
    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSecKillInfo(@PathVariable("skuId") Long skuId) {
        SecKillSkuRedisTo to = secKillService.getSkuSecKillInfo(skuId);
        return R.ok().setData(to);
    }

    @GetMapping("/kill")
    public String secKill(@RequestParam("killId") String killId,
                     @RequestParam("key") String key,
                     @RequestParam("num") Integer num,
                          Model model) {

        //1. 判断是否登录
        String orderSn = secKillService.kill(killId, key, num);

        model.addAttribute("orderSn", orderSn);
        return "success";
    }
}
