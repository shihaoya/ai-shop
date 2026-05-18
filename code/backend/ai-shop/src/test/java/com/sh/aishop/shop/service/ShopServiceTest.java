package com.sh.aishop.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sh.aishop.common.Result;
import com.sh.aishop.common.ResultCode;
import com.sh.aishop.common.entity.Shop;
import com.sh.aishop.common.enums.ShopStatus;
import com.sh.aishop.shop.mapper.ShopMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShopService 单元测试")
class ShopServiceTest {

    @Mock
    private ShopMapper shopMapper;

    @InjectMocks
    private com.sh.aishop.shop.service.ShopService shopService;

    private Shop createTestShop(Long id, Long operatorId, Integer status, Integer isActive) {
        Shop shop = new Shop();
        shop.setId(id);
        shop.setOperatorId(operatorId);
        shop.setName("测试店铺");
        shop.setDescription("测试描述");
        shop.setStatus(status);
        shop.setIsActive(isActive);
        shop.setDeleted(0);
        return shop;
    }

    @Nested
    @DisplayName("getMyShop() 获取我的店铺")
    class GetMyShopTests {

        @Test
        @DisplayName("获取成功 - 店铺存在")
        void getMyShop_Success() {
            Shop shop = createTestShop(1L, 100L, ShopStatus.APPROVED.getCode(), 1);

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);

            Result<?> result = shopService.getMyShop(100L);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNotNull(result.getData());
            assertTrue(result.getData() instanceof java.util.Map);
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> data = (java.util.Map<String, Object>) result.getData();
            assertEquals(true, data.get("hasShop"));
            assertEquals("1", data.get("id"));
            assertEquals("测试店铺", data.get("name"));
        }

        @Test
        @DisplayName("获取成功 - 店铺不存在")
        void getMyShop_NotFound() {
            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            Result<?> result = shopService.getMyShop(100L);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNotNull(result.getData());
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> data = (java.util.Map<String, Object>) result.getData();
            assertEquals(false, data.get("hasShop"));
        }
    }

    @Nested
    @DisplayName("applyShop() 申请店铺")
    class ApplyShopTests {

        @Test
        @DisplayName("申请成功 - 新店铺")
        void applyShop_Success_NewShop() {
            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(shopMapper.insert(any(Shop.class))).thenAnswer(invocation -> {
                Shop s = invocation.getArgument(0);
                return 1;
            });

            Result<?> result = shopService.applyShop(100L, "新店铺", "描述");

            assertEquals(ResultCode.SUCCESS, result.getCode());
            verify(shopMapper).insert(any(Shop.class));
        }

        @Test
        @DisplayName("申请成功 - 更新待审核店铺")
        void applyShop_Success_UpdatePending() {
            Shop existingShop = createTestShop(1L, 100L, ShopStatus.PENDING.getCode(), 0);

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingShop);
            when(shopMapper.updateById(any(Shop.class))).thenReturn(1);

            Result<?> result = shopService.applyShop(100L, "更新店铺", "更新描述");

            assertEquals(ResultCode.SUCCESS, result.getCode());
            verify(shopMapper).updateById(any(Shop.class));
        }

        @Test
        @DisplayName("申请失败 - 已有通过审核的店铺")
        void applyShop_Fail_AlreadyApproved() {
            Shop existingShop = createTestShop(1L, 100L, ShopStatus.APPROVED.getCode(), 1);

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingShop);

            Result<?> result = shopService.applyShop(100L, "新店铺", "描述");

            assertEquals(ResultCode.FAIL, result.getCode());
            assertEquals("已有通过审核的店铺，无需重复申请", result.getMessage());
            verify(shopMapper, never()).insert(any(Shop.class));
        }
    }

    @Nested
    @DisplayName("changeShopStatus() 切换营业状态")
    class ChangeShopStatusTests {

        @Test
        @DisplayName("切换营业状态成功")
        void changeShopStatus_Success() {
            Shop shop = createTestShop(1L, 100L, ShopStatus.APPROVED.getCode(), 0);

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(shopMapper.updateById(any(Shop.class))).thenReturn(1);

            Result<?> result = shopService.changeShopStatus(100L, 1);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            verify(shopMapper).updateById(any(Shop.class));
        }

        @Test
        @DisplayName("切换失败 - 店铺不存在或未通过审核")
        void changeShopStatus_Fail_ShopNotFound() {
            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            Result<?> result = shopService.changeShopStatus(100L, 1);

            assertEquals(ResultCode.SHOP_NOT_FOUND, result.getCode());
            verify(shopMapper, never()).updateById(any(Shop.class));
        }
    }
}