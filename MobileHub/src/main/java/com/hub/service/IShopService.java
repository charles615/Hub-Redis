package com.hub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hub.dto.Result;
import com.hub.entity.Shop;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IShopService extends IService<Shop> {
    Result queryById(Long id) throws InterruptedException;

    Result update(Shop shop);

}
