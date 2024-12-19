package com.hub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hub.dto.Result;
import com.hub.entity.SeckillVoucher;
import com.hub.entity.VoucherOrder;
import com.hub.mapper.VoucherOrderMapper;
import com.hub.service.ISeckillVoucherService;
import com.hub.service.IVoucherOrderService;
import com.hub.utils.RedisWorker;
import com.hub.utils.UserHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private RedisWorker redisWorker;

    @Override
    @Transactional
    public Result seckillVoucher(Long voucherId) {
        // 1.查询优惠券
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
        // 2.判断秒杀是否开始
        if(voucher.getBeginTime().isAfter(LocalDateTime.now())) {
            // 尚未开始
            return Result.fail("Seckill is not ready.");
        }
        // 3.判断秒杀是否已经结束
        if(voucher.getEndTime().isBefore(LocalDateTime.now())) {
            // 尚未开始
            return Result.fail("Seckill is finished.");
        }
        // 4.判断库存是否充足
        if(voucher.getStock() < 1) {
            return  Result.fail("Out of stock.");
        }
        // 5.扣减库存
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1") // set stock = stock - 1
                .eq("voucher_id", voucherId).gt("stock", 0) // where id = ? and stock = ?
                .update();
        if (!success) {
            return  Result.fail("Out of stock.");
        }



        //6.创建订单
        Long userId = UserHolder.getUser().getId();
        // 6.1.查询订单
        int count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
        // 6.2.判断是否存在
        if (count > 0) {
            return Result.fail("Already used.");
        }

        VoucherOrder voucherOrder = new VoucherOrder();
        long orderId = redisWorker.nextId("order");
        voucherOrder.setId(orderId);

        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);
        save(voucherOrder);
        // 7.返回订单id
        return Result.ok(orderId);
    }
}
