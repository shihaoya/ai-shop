package com.sh.aishop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sh.aishop.common.Result;
import com.sh.aishop.common.ResultCode;
import com.sh.aishop.dto.*;
import com.sh.aishop.entity.*;
import com.sh.aishop.entity.enums.*;
import com.sh.aishop.mapper.*;
import com.sh.aishop.util.SnowflakeIdUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService 单元测试")
class AdminServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private ShopMapper shopMapper;

    @Mock
    private InviteCodeMapper inviteCodeMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AdminService adminService;

    private User createTestUser(Long id, String username, Integer role, Integer status) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setNickname("测试用户");
        user.setRole(role);
        user.setStatus(status);
        user.setDeleted(0);
        return user;
    }

    private Shop createTestShop(Long id, Long operatorId, String name, Integer status) {
        Shop shop = new Shop();
        shop.setId(id);
        shop.setOperatorId(operatorId);
        shop.setName(name);
        shop.setDescription("测试店铺描述");
        shop.setStatus(status);
        shop.setIsActive(1);
        shop.setDeleted(0);
        return shop;
    }

    private InviteCode createTestInviteCode(Long creatorId, Integer role, Integer status) {
        InviteCode inviteCode = new InviteCode();
        inviteCode.setId(123456789L);
        inviteCode.setCode("TESTCODE");
        inviteCode.setCreatorId(creatorId);
        inviteCode.setRole(role);
        inviteCode.setStatus(status);
        inviteCode.setDeleted(0);
        return inviteCode;
    }

    private PageRequest createPageRequest() {
        PageRequest request = new PageRequest();
        request.setPage(1);
        request.setPageSize(10);
        return request;
    }

    private PageRequest createPageRequestWithKeyword(String keyword) {
        PageRequest request = new PageRequest();
        request.setPage(1);
        request.setPageSize(10);
        request.setKeyword(keyword);
        return request;
    }

    @Nested
    @DisplayName("getShops() 店铺列表测试")
    class GetShopsTests {

        @Test
        @DisplayName("获取店铺列表成功 - 无关键词")
        void getShops_Success_NoKeyword() {
            PageRequest request = createPageRequest();

            User operator = createTestUser(1L, "operator", RoleEnum.ADMIN.getCode(), UserStatus.NORMAL.getCode());
            List<Shop> shops = new ArrayList<>();
            shops.add(createTestShop(100L, 1L, "店铺A", ShopStatus.APPROVED.getCode()));
            shops.add(createTestShop(101L, 1L, "店铺B", ShopStatus.APPROVED.getCode()));

            when(shopMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(shops);
            when(userMapper.selectById(1L)).thenReturn(operator);

            Result<?> result = adminService.getShops(request);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNotNull(result.getData());
            assertTrue(result.getData() instanceof PageResult);
            PageResult<?> pageResult = (PageResult<?>) result.getData();
            assertEquals(2, pageResult.getRecords().size());
        }

        @Test
        @DisplayName("获取店铺列表成功 - 有关键词过滤")
        void getShops_Success_WithKeyword() {
            PageRequest request = createPageRequestWithKeyword("店铺A");

            User operator = createTestUser(1L, "operator", RoleEnum.ADMIN.getCode(), UserStatus.NORMAL.getCode());
            List<Shop> shops = new ArrayList<>();
            shops.add(createTestShop(100L, 1L, "店铺A", ShopStatus.APPROVED.getCode()));

            when(shopMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(shops);
            when(userMapper.selectById(1L)).thenReturn(operator);

            Result<?> result = adminService.getShops(request);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNotNull(result.getData());
            assertTrue(result.getData() instanceof PageResult);
            PageResult<?> pageResult = (PageResult<?>) result.getData();
            assertEquals(1, pageResult.getRecords().size());
        }

        @Test
        @DisplayName("获取店铺列表成功 - 空列表")
        void getShops_Success_EmptyList() {
            PageRequest request = createPageRequest();

            when(shopMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

            Result<?> result = adminService.getShops(request);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNotNull(result.getData());
            assertTrue(result.getData() instanceof PageResult);
            PageResult<?> pageResult = (PageResult<?>) result.getData();
            assertEquals(0, pageResult.getRecords().size());
            assertEquals(0L, pageResult.getTotal());
        }
    }

    @Nested
    @DisplayName("auditShop() 店铺审核测试")
    class AuditShopTests {

        @Test
        @DisplayName("审核店铺成功 - 批准")
        void auditShop_Approve_Success() {
            Shop shop = createTestShop(100L, 1L, "测试店铺", ShopStatus.PENDING.getCode());

            when(shopMapper.selectById(100L)).thenReturn(shop);
            when(shopMapper.updateById(any(Shop.class))).thenReturn(1);

            Result<?> result = adminService.auditShop(100L, ShopStatus.APPROVED.getCode());

            assertEquals(ResultCode.SUCCESS, result.getCode());
            verify(shopMapper).updateById(any(Shop.class));
        }

        @Test
        @DisplayName("审核店铺成功 - 拒绝")
        void auditShop_Reject_Success() {
            Shop shop = createTestShop(100L, 1L, "测试店铺", ShopStatus.PENDING.getCode());

            when(shopMapper.selectById(100L)).thenReturn(shop);
            when(shopMapper.updateById(any(Shop.class))).thenReturn(1);

            Result<?> result = adminService.auditShop(100L, ShopStatus.REJECTED.getCode());

            assertEquals(ResultCode.SUCCESS, result.getCode());
            verify(shopMapper).updateById(any(Shop.class));
        }

        @Test
        @DisplayName("审核店铺失败 - 店铺不存在")
        void auditShop_ShopNotFound() {
            when(shopMapper.selectById(999L)).thenReturn(null);

            Result<?> result = adminService.auditShop(999L, ShopStatus.APPROVED.getCode());

            assertEquals(ResultCode.SHOP_NOT_FOUND, result.getCode());
            assertEquals("店铺不存在", result.getMessage());
            verify(shopMapper, never()).updateById(any(Shop.class));
        }
    }

    @Nested
    @DisplayName("getUsers() 用户列表测试")
    class GetUsersTests {

        @Test
        @DisplayName("获取用户列表成功 - 无关键词")
        void getUsers_Success_NoKeyword() {
            PageRequest request = createPageRequest();

            List<User> users = new ArrayList<>();
            users.add(createTestUser(2L, "user1", RoleEnum.NORMAL_USER.getCode(), UserStatus.NORMAL.getCode()));
            users.add(createTestUser(3L, "user2", RoleEnum.SHOP_USER.getCode(), UserStatus.PENDING.getCode()));

            when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(users);

            Result<?> result = adminService.getUsers(request);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNotNull(result.getData());
            assertTrue(result.getData() instanceof PageResult);
            PageResult<?> pageResult = (PageResult<?>) result.getData();
            assertEquals(2, pageResult.getRecords().size());
        }

        @Test
        @DisplayName("获取用户列表成功 - 有关键词过滤")
        void getUsers_Success_WithKeyword() {
            PageRequest request = createPageRequestWithKeyword("user1");

            List<User> users = new ArrayList<>();
            users.add(createTestUser(2L, "user1", RoleEnum.NORMAL_USER.getCode(), UserStatus.NORMAL.getCode()));

            when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(users);

            Result<?> result = adminService.getUsers(request);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNotNull(result.getData());
            assertTrue(result.getData() instanceof PageResult);
            PageResult<?> pageResult = (PageResult<?>) result.getData();
            assertEquals(1, pageResult.getRecords().size());
        }

        @Test
        @DisplayName("获取用户列表成功 - 空列表")
        void getUsers_Success_EmptyList() {
            PageRequest request = createPageRequest();

            when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

            Result<?> result = adminService.getUsers(request);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNotNull(result.getData());
            assertTrue(result.getData() instanceof PageResult);
            PageResult<?> pageResult = (PageResult<?>) result.getData();
            assertEquals(0, pageResult.getRecords().size());
            assertEquals(0L, pageResult.getTotal());
        }

        @Test
        @DisplayName("获取用户列表成功 - 分页测试")
        void getUsers_Success_Pagination() {
            PageRequest request = new PageRequest();
            request.setPage(2);
            request.setPageSize(1);

            List<User> users = new ArrayList<>();
            users.add(createTestUser(2L, "user1", RoleEnum.NORMAL_USER.getCode(), UserStatus.NORMAL.getCode()));
            users.add(createTestUser(3L, "user2", RoleEnum.SHOP_USER.getCode(), UserStatus.PENDING.getCode()));

            when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(users);

            Result<?> result = adminService.getUsers(request);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNotNull(result.getData());
            assertTrue(result.getData() instanceof PageResult);
            PageResult<?> pageResult = (PageResult<?>) result.getData();
            assertEquals(1, pageResult.getRecords().size()); // 只返回第二页的1条
            assertEquals(2L, pageResult.getTotal());
        }
    }

    @Nested
    @DisplayName("changeUserStatus() 修改用户状态测试")
    class ChangeUserStatusTests {

        @Test
        @DisplayName("修改用户状态成功 - 冻结用户")
        void changeUserStatus_Freeze_Success() {
            User user = createTestUser(2L, "testuser", RoleEnum.NORMAL_USER.getCode(), UserStatus.NORMAL.getCode());

            when(userMapper.selectById(2L)).thenReturn(user);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            Result<?> result = adminService.changeUserStatus(2L, UserStatus.FROZEN.getCode());

            assertEquals(ResultCode.SUCCESS, result.getCode());
            verify(userMapper).updateById(any(User.class));
        }

        @Test
        @DisplayName("修改用户状态成功 - 解冻用户")
        void changeUserStatus_Unfreeze_Success() {
            User user = createTestUser(2L, "testuser", RoleEnum.NORMAL_USER.getCode(), UserStatus.FROZEN.getCode());

            when(userMapper.selectById(2L)).thenReturn(user);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            Result<?> result = adminService.changeUserStatus(2L, UserStatus.NORMAL.getCode());

            assertEquals(ResultCode.SUCCESS, result.getCode());
            verify(userMapper).updateById(any(User.class));
        }

        @Test
        @DisplayName("修改用户状态失败 - 用户不存在")
        void changeUserStatus_UserNotFound() {
            when(userMapper.selectById(999L)).thenReturn(null);

            Result<?> result = adminService.changeUserStatus(999L, UserStatus.FROZEN.getCode());

            assertEquals(ResultCode.USER_NOT_FOUND, result.getCode());
            assertEquals("用户不存在", result.getMessage());
            verify(userMapper, never()).updateById(any(User.class));
        }

        @Test
        @DisplayName("修改用户状态失败 - 不能操作管理员账号")
        void changeUserStatus_CannotOperateAdmin() {
            User admin = createTestUser(1L, "admin", RoleEnum.ADMIN.getCode(), UserStatus.NORMAL.getCode());

            when(userMapper.selectById(1L)).thenReturn(admin);

            Result<?> result = adminService.changeUserStatus(1L, UserStatus.FROZEN.getCode());

            assertEquals(ResultCode.FAIL, result.getCode());
            assertEquals("不能操作管理员账号", result.getMessage());
            verify(userMapper, never()).updateById(any(User.class));
        }
    }

    @Nested
    @DisplayName("approveUser() 批准用户测试")
    class ApproveUserTests {

        @Test
        @DisplayName("批准用户成功")
        void approveUser_Success() {
            User user = createTestUser(2L, "testuser", RoleEnum.NORMAL_USER.getCode(), UserStatus.PENDING.getCode());

            when(userMapper.selectById(2L)).thenReturn(user);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            Result<?> result = adminService.approveUser(2L);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            verify(userMapper).updateById(any(User.class));
        }

        @Test
        @DisplayName("批准用户失败 - 用户不存在")
        void approveUser_UserNotFound() {
            when(userMapper.selectById(999L)).thenReturn(null);

            Result<?> result = adminService.approveUser(999L);

            assertEquals(ResultCode.USER_NOT_FOUND, result.getCode());
            assertEquals("用户不存在", result.getMessage());
            verify(userMapper, never()).updateById(any(User.class));
        }

        @Test
        @DisplayName("批准用户失败 - 用户不是待审核状态")
        void approveUser_AlreadyApproved() {
            User user = createTestUser(2L, "testuser", RoleEnum.NORMAL_USER.getCode(), UserStatus.NORMAL.getCode());

            when(userMapper.selectById(2L)).thenReturn(user);

            Result<?> result = adminService.approveUser(2L);

            assertEquals(ResultCode.FAIL, result.getCode());
            assertEquals("用户不是待审核状态", result.getMessage());
            verify(userMapper, never()).updateById(any(User.class));
        }
    }

    @Nested
    @DisplayName("getInviteCode() 获取邀请码测试")
    class GetInviteCodeTests {

        @Test
        @DisplayName("获取邀请码成功 - 有邀请码")
        void getInviteCode_HasCode() {
            InviteCode inviteCode = createTestInviteCode(1L, RoleEnum.ADMIN.getCode(), InviteCodeStatus.ACTIVE.getCode());

            when(inviteCodeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(inviteCode);

            Result<?> result = adminService.getInviteCode(1L);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertEquals("TESTCODE", result.getData());
        }

        @Test
        @DisplayName("获取邀请码成功 - 无邀请码")
        void getInviteCode_NoCode() {
            when(inviteCodeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            Result<?> result = adminService.getInviteCode(1L);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNull(result.getData());
        }
    }

    @Nested
    @DisplayName("createInviteCode() 创建邀请码测试")
    class CreateInviteCodeTests {

        @Test
        @DisplayName("创建邀请码成功 - 首次创建")
        void createInviteCode_FirstCreation() {
            when(inviteCodeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(inviteCodeMapper.insert(any(InviteCode.class))).thenReturn(1);

            try (MockedStatic<SnowflakeIdUtil> mockedSnowflake = mockStatic(SnowflakeIdUtil.class)) {
                mockedSnowflake.when(SnowflakeIdUtil::nextId).thenReturn(123456789L);

                Result<?> result = adminService.createInviteCode(1L);

                assertEquals(ResultCode.SUCCESS, result.getCode());
                assertNotNull(result.getData());
                assertTrue(result.getData() instanceof String);
                assertEquals(8, ((String) result.getData()).length()); // 邀请码应该是8位
                verify(inviteCodeMapper).insert(any(InviteCode.class));
            }
        }

        @Test
        @DisplayName("创建邀请码成功 - 重新生成（作废旧码）")
        void createInviteCode_Regenerate() {
            InviteCode oldCode = createTestInviteCode(1L, RoleEnum.ADMIN.getCode(), InviteCodeStatus.ACTIVE.getCode());

            when(inviteCodeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(oldCode);
            when(inviteCodeMapper.updateById(any(InviteCode.class))).thenReturn(1);
            when(inviteCodeMapper.insert(any(InviteCode.class))).thenReturn(1);

            try (MockedStatic<SnowflakeIdUtil> mockedSnowflake = mockStatic(SnowflakeIdUtil.class)) {
                mockedSnowflake.when(SnowflakeIdUtil::nextId).thenReturn(987654321L);

                Result<?> result = adminService.createInviteCode(1L);

                assertEquals(ResultCode.SUCCESS, result.getCode());
                assertNotNull(result.getData());
                verify(inviteCodeMapper).updateById(oldCode); // 旧码被作废
                verify(inviteCodeMapper).insert(any(InviteCode.class)); // 新码被插入
            }
        }
    }
}
