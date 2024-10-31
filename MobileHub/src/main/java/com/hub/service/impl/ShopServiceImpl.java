package com.hub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hub.dto.Result;
import com.hub.entity.Shop;
import com.hub.mapper.ShopMapper;
import com.hub.service.IShopService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.hub.utils.RedisConstants.CACHE_SHOP_KEY;

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

    @Override
    public Result queryById(Long id) {
        // 1.从redis查询商铺缓存
        String shopJson =  stringRedisTemplate.opsForValue().get(CACHE_SHOP_KEY + id);
        // 2.判断是否存在

        // 3.存在，直接返回

        // 4.不存在， 根据id查询数据库

        // 5.数据库不存在，返回错误

        // 6.数据库存在，写入redis

        // 7.返回
        return null;
    }
}
