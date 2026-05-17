package com.sh.aishop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sh.aishop.common.Result;
import com.sh.aishop.common.ResultCode;
import com.sh.aishop.entity.Address;
import com.sh.aishop.mapper.AddressMapper;
import com.sh.aishop.util.SnowflakeIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AddressService {
    @Autowired
    private AddressMapper addressMapper;

    /**
     * 获取收货地址列表
     */
    public Result<?> getAddresses(Long userId) {
        List<Address> addresses = addressMapper.selectList(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId).eq(Address::getDeleted, 0)
                .orderByDesc(Address::getCreatedAt));

        List<Map<String, Object>> result = addresses.stream().map(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId().toString());
            map.put("receiver", a.getName());
            map.put("phone", a.getPhone());
            map.put("province", a.getProvince());
            map.put("city", a.getCity());
            map.put("district", a.getDistrict());
            map.put("detail", a.getDetail());
            map.put("isDefault", a.getIsDefault());
            return map;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    /**
     * 添加收货地址
     */
    @Transactional
    public Result<?> createAddress(Long userId, Map<String, Object> params) {
        // 检查用户地址数量是否已达到上限（5个）
        long addressCount = addressMapper.selectCount(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId).eq(Address::getDeleted, 0));
        if (addressCount >= 5) {
            return Result.fail(ResultCode.ADDRESS_LIMIT_EXCEEDED, "收货地址数量已达到上限（最多5个）");
        }

        Address address = new Address();
        address.setId(SnowflakeIdUtil.nextId());
        address.setUserId(userId);
        // 兼容 receiver 和 name 两种字段名
        Object nameValue = params.get("receiver") != null ? params.get("receiver") : params.get("name");
        address.setName(nameValue != null ? nameValue.toString() : "");
        address.setPhone(params.get("phone") != null ? params.get("phone").toString() : "");
        address.setProvince(params.get("province") != null ? params.get("province").toString() : "");
        address.setCity(params.get("city") != null ? params.get("city").toString() : "");
        address.setDistrict(params.get("district") != null ? params.get("district").toString() : "");
        address.setDetail(params.get("detail") != null ? params.get("detail").toString() : "");
        address.setIsDefault(params.get("isDefault") != null ? Integer.valueOf(params.get("isDefault").toString()) : 0);
        addressMapper.insert(address);

        // 如果是默认地址，取消其他的默认
        if (address.getIsDefault() == 1) {
            addressMapper.update(null, new LambdaUpdateWrapper<Address>()
                    .eq(Address::getUserId, userId)
                    .ne(Address::getId, address.getId())
                    .set(Address::getIsDefault, 0));
        }

        return Result.success(address.getId().toString());
    }

    /**
     * 修改收货地址
     */
    @Transactional
    public Result<?> updateAddress(Long userId, String addressId, Map<String, Object> params) {
        Address address = addressMapper.selectById(Long.valueOf(addressId));
        if (address == null || !address.getUserId().equals(userId)) {
            return Result.fail(ResultCode.ADDRESS_NOT_FOUND, "地址不存在");
        }

        // 兼容 receiver 和 name 两种字段名
        Object nameValue = params.get("receiver") != null ? params.get("receiver") : params.get("name");
        if (nameValue != null) address.setName(nameValue.toString());
        if (params.get("phone") != null) address.setPhone(params.get("phone").toString());
        if (params.get("province") != null) address.setProvince(params.get("province").toString());
        if (params.get("city") != null) address.setCity(params.get("city").toString());
        if (params.get("district") != null) address.setDistrict(params.get("district").toString());
        if (params.get("detail") != null) address.setDetail(params.get("detail").toString());

        addressMapper.updateById(address);

        // 处理默认地址
        if (params.get("isDefault") != null && Integer.valueOf(params.get("isDefault").toString()) == 1) {
            addressMapper.update(null, new LambdaUpdateWrapper<Address>()
                    .eq(Address::getUserId, userId)
                    .ne(Address::getId, address.getId())
                    .set(Address::getIsDefault, 0));
        }

        return Result.success();
    }

    /**
     * 删除收货地址
     */
    @Transactional
    public Result<?> deleteAddress(Long userId, String addressId) {
        Address address = addressMapper.selectById(Long.valueOf(addressId));
        if (address == null || !address.getUserId().equals(userId)) {
            return Result.fail(ResultCode.ADDRESS_NOT_FOUND, "地址不存在");
        }
        addressMapper.deleteById(Long.valueOf(addressId));
        return Result.success();
    }

    /**
     * 设置默认地址
     */
    @Transactional
    public Result<?> setDefaultAddress(Long userId, String addressId) {
        Address address = addressMapper.selectById(Long.valueOf(addressId));
        if (address == null || !address.getUserId().equals(userId)) {
            return Result.fail(ResultCode.ADDRESS_NOT_FOUND, "地址不存在");
        }

        addressMapper.update(null, new LambdaUpdateWrapper<Address>()
                .eq(Address::getUserId, userId)
                .set(Address::getIsDefault, 0));

        address.setIsDefault(1);
        addressMapper.updateById(address);

        return Result.success();
    }
}