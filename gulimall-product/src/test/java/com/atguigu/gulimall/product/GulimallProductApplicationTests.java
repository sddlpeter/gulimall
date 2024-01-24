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
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;



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
