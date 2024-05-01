package com.atguigu.common.to.mq;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SecKillOrderTo {
    private String orderSn;
    private Long promotionSessionId;
    private Long skuId;
    private BigDecimal seckillPrice;
    private Integer num;
    private Long memberId;

}
