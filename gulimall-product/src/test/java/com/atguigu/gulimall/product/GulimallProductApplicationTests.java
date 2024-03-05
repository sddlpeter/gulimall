package com.atguigu.gulimall.product;

//import com.aliyun.oss.OSS;
//import com.aliyun.oss.OSSClient;
//import com.aliyun.oss.OSSClientBuilder;
//import com.aliyun.oss.OSSException;
//import com.aliyun.oss.common.auth.CredentialsProviderFactory;
//import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
//import com.aliyun.oss.model.PutObjectRequest;
//import com.aliyun.oss.model.PutObjectResult;
//import com.aliyuncs.exceptions.ClientException;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.SkuSaleAttrValueDao;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.SkuItemSaleAttrVo;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroupVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Test
    public void test() {
//        List<SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId = attrGroupDao.getAttrGroupWithAttrsBySpuId(2L, 225L);
//        System.out.println(attrGroupWithAttrsBySpuId);

        List<SkuItemSaleAttrVo> saleAttrsBySpuId = skuSaleAttrValueDao.getSaleAttrsBySpuId(2L);
        System.out.println(saleAttrsBySpuId);
    }


    @Test
    public void redisson() {
        System.out.println(redissonClient);
    }


    @Test
    public void testStringRedisTemplate() {
        // hello world
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        // 保存
        ops.set("hello", "world_" + UUID.randomUUID().toString());

        // 查询
        String hello = ops.get("hello");
        System.out.println("保存的数据是：" + hello);
    }


    @Test
    public void testFindPath() {
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("完整路径:", Arrays.asList(catelogPath));
    }



    @Test
    void contextLoads() {
//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setBrandId(1L);
//        brandEntity.setDescript("新手机");

//        brandEntity.setName("xiao mi");
//        brandService.save(brandEntity);
//        System.out.println("saved...");

//        brandService.updateById(brandEntity);
        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));

        list.forEach((item) ->{
            System.out.println(item);
        });


    }

}
