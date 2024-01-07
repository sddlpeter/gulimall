package com.atguigu.gulimall.ware.dao;

import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品库存
 * 
 * @author peter
 * @email sddlpeter@gmail.com
 * @date 2024-01-05 21:25:35
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {
	
}
