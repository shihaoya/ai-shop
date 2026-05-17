package com.sh.aishop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sh.aishop.common.Result;
import com.sh.aishop.common.ResultCode;
import com.sh.aishop.entity.Shop;
import com.sh.aishop.entity.enums.ShopStatus;
import com.sh.aishop.mapper.ShopMapper;
import com.sh.aishop.util.SnowflakeIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class ShopService {
    @Autowired
    private ShopMapper shopMapper;

    /**
     * 获取我的店铺
     */
    public Result<?> getMyShop(Long operatorId) {
        Shop shop = shopMapper.selectOne(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getOperatorId, operatorId)
                .eq(Shop::getDeleted, 0));
        if (shop == null) {
            return Result.success(Collections.singletonMap("hasShop", false));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("hasShop", true);
        result.put("id", shop.getId().toString());
        result.put("name", shop.getName());
        result.put("description", shop.getDescription());
        result.put("status", shop.getStatus());
        result.put("isActive", shop.getIsActive());
        result.put("rejectReason", shop.getRejectReason());
        return Result.success(result);
    }

    /**
     * 申请店铺
     */
    @Transactional
    public Result<?> applyShop(Long operatorId, String name, String description) {
        // 检查是否已有店铺
        Shop existShop = shopMapper.selectOne(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getOperatorId, operatorId)
                .eq(Shop::getDeleted, 0));
        if (existShop != null) {
            // 已通过审核的店铺不能重复申请
            if (existShop.getStatus() == ShopStatus.APPROVED.getCode()) {
                return Result.fail(ResultCode.FAIL, "已有通过审核的店铺，无需重复申请");
            }
            // 重新提交：更新信息并设为待审核，清空拒绝原因
            existShop.setName(name);
            existShop.setDescription(description);
            existShop.setStatus(ShopStatus.PENDING.getCode());
            existShop.setRejectReason(null);
            shopMapper.updateById(existShop);
            return Result.success(existShop.getId().toString());
        }

        Shop shop = new Shop();
        shop.setId(SnowflakeIdUtil.nextId());
        shop.setOperatorId(operatorId);
        shop.setName(name);
        shop.setDescription(description);
        shop.setStatus(ShopStatus.PENDING.getCode());
        shop.setIsActive(0); // 默认为歇业
        shopMapper.insert(shop);

        return Result.success(shop.getId().toString());
    }

    /**
     * 修改营业状态
     */
    @Transactional
    public Result<?> changeShopStatus(Long operatorId, Integer isActive) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        shop.setIsActive(isActive);
        shopMapper.updateById(shop);
        return Result.success();
    }

    /**
     * 重新申请店铺
     */
    @Transactional
    public Result<?> reapplyShop(Long operatorId, String name, String description) {
        Shop shop = shopMapper.selectOne(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getOperatorId, operatorId)
                .eq(Shop::getDeleted, 0));
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在");
        }
        if (shop.getStatus() == ShopStatus.APPROVED.getCode()) {
            return Result.fail(ResultCode.FAIL, "店铺已通过审核，无需重新申请");
        }

        shop.setName(name);
        shop.setDescription(description);
        shop.setStatus(ShopStatus.PENDING.getCode());
        shop.setRejectReason(null);
        shopMapper.updateById(shop);
        return Result.success(shop.getId().toString());
    }

    /**
     * 获取已审核通过的店铺
     */
    private Shop getApprovedShop(Long operatorId) {
        return shopMapper.selectOne(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getOperatorId, operatorId)
                .eq(Shop::getStatus, ShopStatus.APPROVED.getCode())
                .eq(Shop::getDeleted, 0));
    }
}