package com.sh.aishop.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sh.aishop.common.Result;
import com.sh.aishop.common.ResultCode;
import com.sh.aishop.common.entity.Shop;
import com.sh.aishop.common.enums.ShopStatus;
import com.sh.aishop.shop.mapper.ShopMapper;
import com.sh.aishop.util.SnowflakeIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class ShopService implements IShopService {
    @Autowired
    private ShopMapper shopMapper;

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

    @Transactional
    public Result<?> applyShop(Long operatorId, String name, String description) {
        Shop existShop = shopMapper.selectOne(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getOperatorId, operatorId)
                .eq(Shop::getDeleted, 0));
        if (existShop != null) {
            if (existShop.getStatus() == ShopStatus.APPROVED.getCode()) {
                return Result.fail(ResultCode.FAIL, "已有通过审核的店铺，无需重复申请");
            }
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
        shop.setIsActive(0);
        shopMapper.insert(shop);

        return Result.success(shop.getId().toString());
    }

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

    Shop getApprovedShop(Long operatorId) {
        return shopMapper.selectOne(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getOperatorId, operatorId)
                .eq(Shop::getDeleted, 0)
                .eq(Shop::getStatus, ShopStatus.APPROVED.getCode()));
    }
}