package com.atguigu.gulimall.order.vo;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSubmitVo {
    private Long addrId;
    private Integer payType;
    // 无需提交需要购买的商品，去购物车再获取一遍

    private String orderToken; // 防重令牌
    private BigDecimal payPrice; // 应付价格 验价
    private String note; // 订单备注

    // 用户相关信息都在session里，去session里取


}
