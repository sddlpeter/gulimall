package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.Catalog2Vo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author peter
 * @email sddlpeter@gmail.com
 * @date 2024-01-05 15:11:55
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIds(List<Long> list);

    Long[] findCatelogPath(Long catelogId);

    void updateCascade(CategoryEntity category);


    List<CategoryEntity> getLevel1Categories();

    Map<String, List<Catalog2Vo>> getCatalogJson();
}

