package com.sh.aishop.user.service;

import com.sh.aishop.common.Result;
import com.sh.aishop.common.ResultCode;
import com.sh.aishop.common.entity.Address;
import com.sh.aishop.mapper.AddressMapper;
import com.sh.aishop.util.SnowflakeIdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressService {
    @Autowired
    private AddressMapper addressMapper;

    public Result<?> getAddresses(Long userId) {
        List<Address> addresses = addressMapper.selectList(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId).eq(Address::getDeleted, 0)
                .orderByDesc(Address::getCreatedAt));

        List<java.util.Map<String, Object>> result = addresses.stream().map(a -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
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

    @Transactional
    public Result<?> createAddress(Long userId, java.util.Map<String, Object> params) {
        LambdaQueryWrapper<Address> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(Address::getUserId, userId).eq(Address::getDeleted, 0);
        long addressCount = addressMapper.selectCount(countWrapper);
        if (addressCount >= 5) {
            return Result.fail(ResultCode.ADDRESS_LIMIT_EXCEEDED, "收货地址数量已达到上限（最多5个）");
        }

        Address address = new Address();
        address.setId(SnowflakeIdUtil.nextId());
        address.setUserId(userId);
        Object nameValue = params.get("receiver") != null ? params.get("receiver") : params.get("name");
        address.setName(nameValue != null ? nameValue.toString() : "");
        address.setPhone(params.get("phone") != null ? params.get("phone").toString() : "");
        address.setProvince(params.get("province") != null ? params.get("province").toString() : "");
        address.setCity(params.get("city") != null ? params.get("city").toString() : "");
        address.setDistrict(params.get("district") != null ? params.get("district").toString() : "");
        address.setDetail(params.get("detail") != null ? params.get("detail").toString() : "");
        address.setIsDefault(params.get("isDefault") != null ? Integer.valueOf(params.get("isDefault").toString()) : 0);
        addressMapper.insert(address);

        if (address.getIsDefault() == 1) {
            addressMapper.update(null, new LambdaUpdateWrapper<Address>()
                    .eq(Address::getUserId, userId)
                    .ne(Address::getId, address.getId())
                    .set(Address::getIsDefault, 0));
        }

        return Result.success(address.getId().toString());
    }

    @Transactional
    public Result<?> updateAddress(Long userId, Long addressId, java.util.Map<String, Object> params) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || !userId.equals(address.getUserId())) {
            return Result.fail(ResultCode.ADDRESS_NOT_FOUND, "地址不存在");
        }

        Object nameValue = params.get("receiver") != null ? params.get("receiver") : params.get("name");
        if (nameValue != null) address.setName(nameValue.toString());
        if (params.get("phone") != null) address.setPhone(params.get("phone").toString());
        if (params.get("province") != null) address.setProvince(params.get("province").toString());
        if (params.get("city") != null) address.setCity(params.get("city").toString());
        if (params.get("district") != null) address.setDistrict(params.get("district").toString());
        if (params.get("detail") != null) address.setDetail(params.get("detail").toString());

        addressMapper.updateById(address);

        if (params.get("isDefault") != null && Integer.valueOf(params.get("isDefault").toString()) == 1) {
            addressMapper.update(null, new LambdaUpdateWrapper<Address>()
                    .eq(Address::getUserId, userId)
                    .ne(Address::getId, addressId)
                    .set(Address::getIsDefault, 0));
        }

        return Result.success();
    }

    @Transactional
    public Result<?> deleteAddress(Long userId, Long addressId) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || !userId.equals(address.getUserId())) {
            return Result.fail(ResultCode.ADDRESS_NOT_FOUND, "地址不存在");
        }
        addressMapper.deleteById(addressId);
        return Result.success();
    }

    @Transactional
    public Result<?> setDefaultAddress(Long userId, Long addressId) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || !userId.equals(address.getUserId())) {
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