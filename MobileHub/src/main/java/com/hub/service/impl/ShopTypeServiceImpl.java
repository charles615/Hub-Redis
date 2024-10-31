package com.hub.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hub.dto.Result;
import com.hub.entity.ShopType;
import com.hub.mapper.ShopTypeMapper;
import com.hub.service.IShopTypeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.hub.utils.RedisConstants.CACHE_SHOP_TYPE_KEY;
import static com.hub.utils.RedisConstants.CACHE_SHOP_TYPE_TTL;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryTypeList() {

        List<String> list = stringRedisTemplate.opsForList().range(CACHE_SHOP_TYPE_KEY, 0, -1);
        if (!CollectionUtil.isEmpty(list)) {
            System.out.println("The list exists.");
            List<ShopType> listBean = list.stream()
                    .map(item -> JSONUtil.toBean(item, ShopType.class))
                    .collect(Collectors.toList());
            return Result.ok(listBean);
        } else {
            System.out.println("The list does not exist.");
        }
        List<ShopType> typeList = query().orderByAsc("sort").list();

        if (CollectionUtil.isEmpty(typeList)) {
            return Result.fail("Can not find.");
        }
        // 6.数据库存在，写入redis
        List<String> listString = typeList.stream()
                        .map(JSONUtil::toJsonStr)
                        .collect(Collectors.toList());
        stringRedisTemplate.opsForList().rightPushAll(CACHE_SHOP_TYPE_KEY, listString);
        stringRedisTemplate.expire(CACHE_SHOP_TYPE_KEY, CACHE_SHOP_TYPE_TTL, TimeUnit.MINUTES);
        // 7.返回
        return Result.ok(typeList);

    }
}
