package com.sh.aishop.order.service;

import com.sh.aishop.common.Result;
import com.sh.aishop.common.dto.PageRequest;

import java.util.Map;

public interface IOrderService {
    Result<?> getShopOrders(Long operatorId, PageRequest pageRequest, Integer status);
    Result<?> confirmOrder(Long operatorId, Long orderId);
    Result<?> shipOrder(Long operatorId, Long orderId, Map<String, Object> params);
    Result<?> closeShopOrder(Long operatorId, Long orderId, String reason);
    Result<?> completeShopOrder(Long operatorId, Long orderId);
}
