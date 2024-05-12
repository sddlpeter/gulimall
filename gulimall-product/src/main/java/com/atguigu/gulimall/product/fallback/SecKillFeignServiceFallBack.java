package com.atguigu.gulimall.product.fallback;

import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.feign.SecKillFeignService;
import org.springframework.stereotype.Component;

@Component
public class SecKillFeignServiceFallBack implements SecKillFeignService {
    @Override
    public R getSkuSecKillInfo(Long skuId) {
        return R.error(BizCodeEnum.TOO_MANY_REQUEST.getCode(), BizCodeEnum.TOO_MANY_REQUEST.getMsg());
    }
}
