package com.atguigu.gulimall.product.vo;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.entity.SpuInfoEntity;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@ToString
@Data
public class SkuItemVo {
    //1. sku的基本信息获取
    SkuInfoEntity info;

    boolean  hasStock = true;

    //2. sku的图片信息
    List<SkuImagesEntity> images;

    //3. 获取spu的销售属性组合
    List<SkuItemSaleAttrVo> saleAttr;

    //4. 获取spu的介绍
    SpuInfoDescEntity desc;

    //5. 获取spu规格参数信息
    List<SpuItemAttrGroupVo> groupAttrs;

    SecKillInfoVo secKillInfoVo; // 当前商品的秒杀优惠信息


}
