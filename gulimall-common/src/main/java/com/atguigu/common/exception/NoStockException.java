package com.atguigu.common.exception;

import lombok.Getter;
import lombok.Setter;

public class NoStockException extends RuntimeException {
    @Getter @Setter
    private Long skuId;
    public NoStockException(Long skuId) {

        super("没有足够的库存了:" + skuId);
    }

    public NoStockException() {

        super("没有足够的库存了");
    }
}
