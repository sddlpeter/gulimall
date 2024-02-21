package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catalog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    // Map<String, Object> cache = new HashMap<>();

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {

        //1. 查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //2. 组装父子的树形结构
        //2.1 找到所有的一级分类
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == 0;
        }).map((menu) -> {
            menu.setChildren(getChildren(menu, entities));
            return menu;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> list) {
        // TODO 1. 检查菜单是否被引用

        // 逻辑删除
        baseMapper.deleteBatchIds(list);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);

        return parentPath.toArray(new Long[parentPath.size()]);
    }


    // 级联更细腻所有关联的数据

//    @Caching(evict = {
//            @CacheEvict(value = "category", key="'getLevel1Categories'"),
//            @CacheEvict(value = "category", key="'getCatalogJson'")
//    })
    @CacheEvict(value = "category", allEntries = true) // 失效模式
    // @CachePut // 双写模式，当前接口或方法没有返回值，双写模式需要将方法返回值放入缓存
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    // 每一个需要缓存的数据，需要指定放到哪个名字的缓存
    @Cacheable(value = {"category"}, key = "#root.method.name")  // 代表当前方法的结果需要缓存，如果缓存中有，方法不调用;若缓存没有，则调用方法，将方法结果放入缓存
    @Override
    public List<CategoryEntity> getLevel1Categories() {
        System.out.println("getLevel1Categories...");
        List<CategoryEntity> entities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return entities;
    }


    @Cacheable(value = "category", key= "#root.methodName")
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {

        System.out.println("查询了数据库.....");
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        List<CategoryEntity> level1Categories = getParentCid(selectList, 0L);

        Map<String, List<Catalog2Vo>> parentCid = level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 1. 每一个的一级分类，查到这个一级分类的二级分类
            List<CategoryEntity> entities = getParentCid(selectList, v.getCatId());

            // 2. 封装上面的结果
            List<Catalog2Vo> catalog2Vos = null;
            if (entities != null) {
                catalog2Vos = entities.stream().map(l2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    // 1. 找当前二级分类的三级分类 封装成vo
                    List<CategoryEntity> level3Catalog = getParentCid(selectList, l2.getCatId());

                    if (level3Catalog != null) {
                        List<Catalog2Vo.Catalog3Vo> collect = level3Catalog.stream().map(l3 -> {
                            //2. 封装成指定格式
                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catalog3Vo;
                        }).collect(Collectors.toList());
                        catalog2Vo.setCatalog3List(collect);
                    }
                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));

        //3. 查到的数据，将对象转为json放入缓存

        return parentCid;
    }

    // TODO 产生堆外内存溢出： OutOfDirectMemoryError
    //1. springboot2.0 以后默认使用lettuce作为操作redis的客户端,使用netty进行网络通信
    //2. lettuce的bug导致netty堆外内存溢出
    // 解决方案：1.升级lettuce客户端    2.切换使用jedis
    // @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson2() {
        //  给缓存中放json字符串，拿出的json字符串，还可以逆转成能用的对象类型，这就是序列化和反序列化

        // 1. 空结果缓存，为了解决缓存的穿透问题 2.设置过期事件，解决缓存雪崩问题  3.加锁，解决缓存击穿问题

        //1. 加入缓存逻辑, 缓存中存的是json字符串, json是跨语言，跨平台
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {
            // 2. 缓存中没有, 查询数据库
            System.out.println("缓存不命中......将要查询数据库.....");
            Map<String, List<Catalog2Vo>> catalogJsonFromDb = getCatalogJsonFromDbWithRedisLock();

            return catalogJsonFromDb;
        }
        System.out.println("缓存命中......直接返回.....");
        Map<String, List<Catalog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2Vo>>>() {
        });
        return result;
    }


    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedissonLock() {

        RLock lock = redisson.getLock("catalogJson-lock");
        lock.lock();

        Map<String, List<Catalog2Vo>> dataFromDb;
        try {
            dataFromDb = getDataFromDb();
        } finally {
            lock.unlock();
        }
        return dataFromDb;

    }


    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedisLock() {

        //1. 占分布式锁，去redis占坑
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("获取分布式锁成功...");
            // 加锁成功, 执行业务
            // 2. 设置过期时间, 必须和加锁是同一个原子操作
            // redisTemplate.expire("lock", 30, TimeUnit.SECONDS);

            Map<String, List<Catalog2Vo>> dataFromDb;
            try {
                dataFromDb = getDataFromDb();

            } finally {
                String script = "if redis.call('get', KEYS[1]) == ARGV[1]\n" +
                        "    then\n" +
                        "        return redis.call('del', KEYS[1])\n" +
                        "    else\n" +
                        "        return 0\n" +
                        "end";
                // 删除锁 -- 原子操作
                Long lock1 = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            }
            // redisTemplate.delete("lock"); // 删除锁

            // 获取值对比 + 对比成功删除 = 原子操作 --> lua脚本解锁
//            String lockValue = redisTemplate.opsForValue().get("lock");
//            if (uuid.equals(lockValue)) {
//                redisTemplate.delete("lock");
//            }

            return dataFromDb;
        } else {
            System.out.println("获取分布失败成功...等待重试...");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // 加锁失败, 重试
            // 休眠100ms
            return getCatalogJsonFromDbWithRedisLock();  // 自旋的方式
        }
    }

    private Map<String, List<Catalog2Vo>> getDataFromDb() {
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)) {
            // 缓存不为空，直接返回缓存查询结果
            Map<String, List<Catalog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });
            return result;
        }

        System.out.println("查询了数据库.....");

        List<CategoryEntity> selectList = baseMapper.selectList(null);

        List<CategoryEntity> level1Categories = getParentCid(selectList, 0L);

        Map<String, List<Catalog2Vo>> parentCid = level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 1. 每一个的一级分类，查到这个一级分类的二级分类
            List<CategoryEntity> entities = getParentCid(selectList, v.getCatId());

            // 2. 封装上面的结果
            List<Catalog2Vo> catalog2Vos = null;
            if (entities != null) {
                catalog2Vos = entities.stream().map(l2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    // 1. 找当前二级分类的三级分类 封装成vo
                    List<CategoryEntity> level3Catalog = getParentCid(selectList, l2.getCatId());

                    if (level3Catalog != null) {
                        List<Catalog2Vo.Catalog3Vo> collect = level3Catalog.stream().map(l3 -> {
                            //2. 封装成指定格式
                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catalog3Vo;
                        }).collect(Collectors.toList());
                        catalog2Vo.setCatalog3List(collect);
                    }
                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));

        //3. 查到的数据，将对象转为json放入缓存
        String jsonString = JSON.toJSONString(parentCid);
        redisTemplate.opsForValue().set("catalogJSON", jsonString, 1, TimeUnit.DAYS);
        return parentCid;
    }

    // 从数据库查询并封装分类数据
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithLocalLock() {

        // 只要是同一把锁，就能锁住需要这个锁的所有线程
        // 1. synchronized(this): springboot所有的组件在容器中都是单例的
        // TODO 本地锁： synchronized, JUC(Lock), 在分布式情况下，需要使用分布式锁
        synchronized (this) {
            // 得到锁以后，应该再去缓存中确定一次，如果没有才需要继续查询
            String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
            if (!StringUtils.isEmpty(catalogJSON)) {
                // 缓存不为空，直接返回缓存查询结果
                Map<String, List<Catalog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2Vo>>>() {
                });
                return result;
            }

            System.out.println("查询了数据库.....");

            List<CategoryEntity> selectList = baseMapper.selectList(null);
            // 查所有1分类
            List<CategoryEntity> level1Categories = getParentCid(selectList, 0L);

            Map<String, List<Catalog2Vo>> parentCid = level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                // 1. 每一个的一级分类，查到这个一级分类的二级分类
                List<CategoryEntity> entities = getParentCid(selectList, v.getCatId());

                // 2. 封装上面的结果
                List<Catalog2Vo> catalog2Vos = null;
                if (entities != null) {
                    catalog2Vos = entities.stream().map(l2 -> {
                        Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                        // 1. 找当前二级分类的三级分类 封装成vo
                        List<CategoryEntity> level3Catalog = getParentCid(selectList, l2.getCatId());

                        if (level3Catalog != null) {
                            List<Catalog2Vo.Catalog3Vo> collect = level3Catalog.stream().map(l3 -> {
                                //2. 封装成指定格式
                                Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                                return catalog3Vo;
                            }).collect(Collectors.toList());
                            catalog2Vo.setCatalog3List(collect);
                        }
                        return catalog2Vo;
                    }).collect(Collectors.toList());
                }
                return catalog2Vos;
            }));

            //3. 查到的数据，将对象转为json放入缓存
            String jsonString = JSON.toJSONString(parentCid);
            redisTemplate.opsForValue().set("catalogJSON", jsonString, 1, TimeUnit.DAYS);
            return parentCid;
        }
    }

    private List<CategoryEntity> getParentCid(List<CategoryEntity> selectList, Long parentCid) {
        // return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid() == parentCid).collect(Collectors.toList());
        return collect;
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }

        return paths;
    }

    // 递归查找所有一级分类菜单的子菜单
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map(categoryEntity -> {
            // 1. 递归找到子菜单
            categoryEntity.setChildren(getChildren(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            // 2. 排序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());


        return children;
    }

}