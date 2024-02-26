package com.atguigu.gulimall.search.vo;


import lombok.Data;

import java.util.List;


// 封装页面所有可能传过来的数据
@Data
public class SearchParam {
    private String keyword;
    private Long catalog3Id;

    private String sort;
    private Integer hasStock;  // 是否显示有货
    private String skuPrice;
    private List<Long> brandId; // 按照品牌进行查询，可以多选
    private List<String> attrs;  // 按照属性筛选

    private Integer pageNum = 1; // 页码




}
