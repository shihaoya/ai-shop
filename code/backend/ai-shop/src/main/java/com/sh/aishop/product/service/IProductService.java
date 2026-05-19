package com.sh.aishop.product.service;

import com.sh.aishop.common.Result;
import com.sh.aishop.common.dto.PageRequest;

import java.util.Map;

public interface IProductService {
    Result<?> getProducts(Long operatorId, PageRequest pageRequest);
    Result<?> createProduct(Long operatorId, Map<String, Object> params);
    Result<?> getProduct(Long operatorId, Long productId);
    Result<?> updateProduct(Long operatorId, Long productId, Map<String, Object> params);
    Result<?> deleteProduct(Long operatorId, Long productId);
}
