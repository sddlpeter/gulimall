package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.gulimall.ware.vo.OrderItemVo;
import com.atguigu.gulimall.ware.vo.SkuHasStockVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1. 判断如果还没有这个库存记录，那就是新增操作
        List<WareSkuEntity> entities = wareSkuDao.selectList(
                new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (entities == null || entities.size() == 0) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStockLocked(0);
            // TODO 远程查询sku的名字, 如果失败，整个事务无需回滚
            //1. 自己catch异常
            //2. 还可以让异常出现以后不回滚
            try {
                R info = productFeignService.info(skuId);
                if (info.getCode() == 0) {
                    Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
                    wareSkuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {

            }
            wareSkuDao.insert(wareSkuEntity);
        } else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> collect = skuIds.stream().map(sku -> {
            SkuHasStockVo vo = new SkuHasStockVo();
            // 查询当前sku的总库存
            Long count = baseMapper.getSkuStock(sku);
            vo.setSkuId(sku);
            vo.setHasStock(count == null || count == 0 ? false : true);
            return vo;
        }).collect(Collectors.toList());
        return collect;
    }

    // 为某个订单，锁定库存
    @Transactional(rollbackFor = NoStockException.class)
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
        //1. 按照下单的收货地址，找到一个就近的仓库，锁定库存

        // 1. 找到每个商品在哪个仓库有库存
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            List<Long> wareIds = wareSkuDao.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIds);
            return stock;
        }).collect(Collectors.toList());

        Boolean allLock = true;
        // 2. 锁定库存
        for (SkuWareHasStock hasStock : collect) {
            Boolean skuStocked = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                // 没有任何仓库有这个商品的库存
                throw new NoStockException(skuId);
            }
            for (Long wareId : wareIds) {
                // 成功返回1，否则返回0
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, hasStock.getNum());
                if(count == 1) {
                    // 锁库存成功
                    skuStocked = true;
                    break;
                } else {
                    // 当前仓库失败，重试下一个仓库
                }
            }
            if (skuStocked == false) {
                // 当前商品，所有仓库都没锁住
                throw new NoStockException(skuId);
            }
        }

        //3. 肯定都是锁定成功的
        return true;
    }

    @Data
    class SkuWareHasStock{
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }

}