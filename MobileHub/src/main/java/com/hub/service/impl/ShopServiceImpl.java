package com.hub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hub.dto.Result;
import com.hub.entity.Shop;
import com.hub.mapper.ShopMapper;
import com.hub.service.IShopService;
import com.hub.utils.CacheClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

import static com.hub.utils.RedisConstants.CACHE_SHOP_KEY;
import static com.hub.utils.RedisConstants.CACHE_SHOP_TTL;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {


    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CacheClient cacheClient;


    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("Id is null");
        }
        // 1. 更新数据库
        updateById(shop);
        // 2.删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id);

        return Result.ok();
    }



    @Override
    public Result queryById(Long id) throws InterruptedException {
        // 缓存穿透
//        Shop shop = cacheClient.queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 解决缓存击穿 -  互斥锁
//        Shop shop = queryWithMutex(id);

        //  解决缓存击穿 逻辑过期
        Shop shop = cacheClient.queryWithLogicalExpire(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);

        if (shop == null) {return Result.fail("Shop does not exist.");}

        // 7.返回
        return Result.ok(shop);
    }

}
