package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SubmitOrderResponseVo {
    private OrderEntity order;
    private Integer code; // 错误状态码 0 成功

}
