package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catalog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redisson;

    @Autowired
    StringRedisTemplate redisTemplate;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {

        //1. 查出所有一级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categories();

        model.addAttribute("categories", categoryEntities);
        return "index";

    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catalog2Vo>> getCatalogJson() {

        Map<String, List<Catalog2Vo>> catalogJson = categoryService.getCatalogJson();
        return catalogJson;
    }


    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        // 1. 调用getlock获取一把锁，只要锁名字一样就是同一把锁
        RLock lock = redisson.getLock("my-lock");
        // 2. 加锁
        lock.lock(); // 阻塞式等待, 默认加锁都是30s
        // 1) 锁的自动续期, 如果业务超长，运行期间自动给锁续上新的30s
        // 2) 加锁的业务只要运行完成，就不会自动续期，即使不手动解锁，默认30s后自动删除

        // lock.lock(10, TimeUnit.SECONDS);
        try {
            System.out.println("加锁成功，执行业务..." + Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (Exception e) {

        }
        finally {
            // 3. 解锁
            System.out.println("释放锁..." + Thread.currentThread().getId());
            lock.unlock();
        }
        return "hello";
    }

    // 保证一定能读取到最新数据，修改期间，写锁是一个排他锁，读锁是一个共享锁
    // 写锁没释放，读锁就必须等待
    @GetMapping("/write")
    @ResponseBody
    public String writeValue() {

        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        String s = "";
        RLock rLock = lock.writeLock();
        try {
            // 1. 改写数据加写锁，读取数据加读锁
            rLock.lock();
            System.out.println("写锁加锁成功... " + Thread.currentThread().getId());
            s = UUID.randomUUID().toString();
            Thread.sleep(30 * 1000);
            redisTemplate.opsForValue().set("writeValue", s);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            rLock.unlock();
            System.out.println("写锁释放... " + Thread.currentThread().getId());
        }
        return s;

    }

    @GetMapping("/read")
    @ResponseBody
    public String readValue() {
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        String s = "";
        // 加读锁
        RLock rLock = lock.readLock();
        System.out.println("读锁加锁成功... " + Thread.currentThread().getId());
        rLock.lock();
        try {
            s = redisTemplate.opsForValue().get("writeValue");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            rLock.unlock();
            System.out.println("读锁释放... " + Thread.currentThread().getId());
        }
        return s;
    }

    @GetMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
        // park.acquire(); // 获取一个信号，占用一个车位
        boolean b = park.tryAcquire();
        if (b) {
            // 执行业务
        } else {
            return "error";
        }
        return "ok=>" + b;
    }

    @GetMapping("/go")
    @ResponseBody
    public String go() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
        park.release();// 释放一个车位
        return "ok";
    }


    @GetMapping("/lockDoor")
    @ResponseBody
    public String lockDoor() throws InterruptedException {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.trySetCount(5);
        door.await();

        return "放假了...";
    }

    @GetMapping("/gogogo/{id}")
    @ResponseBody
    public String gogogo(@PathVariable("id") Long id) {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.countDown();

        return id + "班的人都走了...";
    }
}
