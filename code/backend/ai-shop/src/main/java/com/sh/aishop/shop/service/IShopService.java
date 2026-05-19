package com.sh.aishop.shop.service;

import com.sh.aishop.common.Result;
import com.sh.aishop.common.entity.Shop;

public interface IShopService {
    Result<?> getMyShop(Long operatorId);
    Result<?> applyShop(Long operatorId, String name, String description);
    Result<?> changeShopStatus(Long operatorId, Integer isActive);
}