package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.util.List;

@Data
public class WareSkuLockVo {
    private String OrderSn;  //订单号
    private List<OrderItemVo> locks; //需要锁住的订单信息

}
