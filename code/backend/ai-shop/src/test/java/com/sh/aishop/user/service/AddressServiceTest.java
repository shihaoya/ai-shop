package com.sh.aishop.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sh.aishop.common.Result;
import com.sh.aishop.common.ResultCode;
import com.sh.aishop.common.entity.Address;
import com.sh.aishop.user.mapper.AddressMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AddressService 单元测试")
class AddressServiceTest {

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private com.sh.aishop.user.service.AddressService addressService;

    private Address createTestAddress(Long id, Long userId, Integer isDefault) {
        Address address = new Address();
        address.setId(id);
        address.setUserId(userId);
        address.setName("张三");
        address.setPhone("13800138000");
        address.setProvince("广东省");
        address.setCity("深圳市");
        address.setDistrict("南山区");
        address.setDetail("科技园");
        address.setIsDefault(isDefault);
        address.setDeleted(0);
        return address;
    }

    @Nested
    @DisplayName("getAddresses() 获取地址列表")
    class GetAddressesTests {

        @Test
        @DisplayName("获取地址列表成功")
        void getAddresses_Success() {
            Address addr1 = createTestAddress(1L, 200L, 1);
            Address addr2 = createTestAddress(2L, 200L, 0);

            when(addressMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(addr1, addr2));

            Result<?> result = addressService.getAddresses(200L);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNotNull(result.getData());
            assertTrue(result.getData() instanceof List);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) result.getData();
            assertEquals(2, list.size());
        }

        @Test
        @DisplayName("获取地址列表成功 - 无地址")
        void getAddresses_Success_Empty() {
            when(addressMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            Result<?> result = addressService.getAddresses(200L);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNotNull(result.getData());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) result.getData();
            assertTrue(list.isEmpty());
        }
    }

    @Nested
    @DisplayName("createAddress() 创建地址")
    class CreateAddressTests {

        @Test
        @DisplayName("创建地址成功 - 非默认地址")
        void createAddress_Success_NonDefault() {
            when(addressMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);
            when(addressMapper.insert(any(Address.class))).thenReturn(1);

            Map<String, Object> params = new HashMap<>();
            params.put("receiver", "李四");
            params.put("phone", "13900139000");
            params.put("province", "北京市");
            params.put("city", "北京市");
            params.put("district", "朝阳区");
            params.put("detail", "CBD");
            params.put("isDefault", 0);

            Result<?> result = addressService.createAddress(200L, params);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            verify(addressMapper).insert(any(Address.class));
        }

        @Test
        @DisplayName("创建地址失败 - 超过数量上限")
        void createAddress_Fail_LimitExceeded() {
            when(addressMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

            Map<String, Object> params = new HashMap<>();
            params.put("receiver", "李四");
            params.put("phone", "13900139000");
            params.put("province", "北京市");
            params.put("city", "北京市");
            params.put("district", "朝阳区");
            params.put("detail", "CBD");

            Result<?> result = addressService.createAddress(200L, params);

            assertEquals(ResultCode.ADDRESS_LIMIT_EXCEEDED, result.getCode());
            assertEquals("收货地址数量已达到上限（最多5个）", result.getMessage());
            verify(addressMapper, never()).insert(any(Address.class));
        }
    }

    @Nested
    @DisplayName("updateAddress() 更新地址")
    class UpdateAddressTests {

        @Test
        @DisplayName("更新地址成功")
        void updateAddress_Success() {
            Address address = createTestAddress(1L, 200L, 0);

            when(addressMapper.selectById(1L)).thenReturn(address);
            when(addressMapper.updateById(any(Address.class))).thenReturn(1);

            Map<String, Object> params = new HashMap<>();
            params.put("receiver", "更新姓名");
            params.put("phone", "13888888888");

            Result<?> result = addressService.updateAddress(200L, 1L, params);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            verify(addressMapper).updateById(any(Address.class));
        }

        @Test
        @DisplayName("更新地址失败 - 地址不属于该用户")
        void updateAddress_Fail_NotOwner() {
            Address address = createTestAddress(1L, 999L, 0);

            when(addressMapper.selectById(1L)).thenReturn(address);

            Result<?> result = addressService.updateAddress(200L, 1L, new HashMap<>());

            assertEquals(ResultCode.ADDRESS_NOT_FOUND, result.getCode());
            verify(addressMapper, never()).updateById(any(Address.class));
        }

    }

    @Nested
    @DisplayName("deleteAddress() 删除地址")
    class DeleteAddressTests {

        @Test
        @DisplayName("删除地址成功")
        void deleteAddress_Success() {
            Address address = createTestAddress(1L, 200L, 0);

            when(addressMapper.selectById(1L)).thenReturn(address);
            when(addressMapper.deleteById(1L)).thenReturn(1);

            Result<?> result = addressService.deleteAddress(200L, 1L);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            verify(addressMapper).deleteById(1L);
        }

        @Test
        @DisplayName("删除地址失败 - 地址不属于该用户")
        void deleteAddress_Fail_NotOwner() {
            Address address = createTestAddress(1L, 999L, 0);

            when(addressMapper.selectById(1L)).thenReturn(address);

            Result<?> result = addressService.deleteAddress(200L, 1L);

            assertEquals(ResultCode.ADDRESS_NOT_FOUND, result.getCode());
            verify(addressMapper, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("setDefaultAddress() 设置默认地址")
    class SetDefaultAddressTests {

        @Test
        @DisplayName("设置默认地址失败 - 地址不属于该用户")
        void setDefaultAddress_Fail_NotOwner() {
            Address address = createTestAddress(1L, 999L, 0);

            when(addressMapper.selectById(1L)).thenReturn(address);

            Result<?> result = addressService.setDefaultAddress(200L, 1L);

            assertEquals(ResultCode.ADDRESS_NOT_FOUND, result.getCode());
        }
    }
}