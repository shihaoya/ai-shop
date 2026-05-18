package com.sh.aishop.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sh.aishop.common.Result;
import com.sh.aishop.common.ResultCode;
import com.sh.aishop.common.entity.InviteCode;
import com.sh.aishop.common.entity.User;
import com.sh.aishop.common.enums.InviteCodeStatus;
import com.sh.aishop.common.enums.RoleEnum;
import com.sh.aishop.common.enums.UserStatus;
import com.sh.aishop.auth.mapper.InviteCodeMapper;
import com.sh.aishop.user.mapper.UserMapper;
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
@DisplayName("UserService 单元测试")
class UserServiceTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private InviteCodeMapper inviteCodeMapper;

    @InjectMocks
    private com.sh.aishop.user.service.UserService userService;

    private User createTestUser(Long id, Integer role, Integer status) {
        User user = new User();
        user.setId(id);
        user.setUsername("testuser");
        user.setNickname("测试用户");
        user.setPassword("encoded_password");
        user.setRole(role);
        user.setStatus(status);
        user.setDeleted(0);
        return user;
    }

    private InviteCode createTestInviteCode(Long creatorId, Integer role, Integer status) {
        InviteCode code = new InviteCode();
        code.setId(1L);
        code.setCode("TESTCODE");
        code.setCreatorId(creatorId);
        code.setRole(role);
        code.setStatus(status);
        code.setDeleted(0);
        return code;
    }

    @Nested
    @DisplayName("getUserInfo() 获取用户信息")
    class GetUserInfoTests {

        @Test
        @DisplayName("获取用户信息成功")
        void getUserInfo_Success() {
            User user = createTestUser(200L, RoleEnum.NORMAL_USER.getCode(), UserStatus.NORMAL.getCode());
            when(userMapper.selectById(200L)).thenReturn(user);

            Result<?> result = userService.getUserInfo(200L);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNotNull(result.getData());
        }

        @Test
        @DisplayName("获取用户信息失败 - 用户不存在")
        void getUserInfo_NotFound() {
            when(userMapper.selectById(999L)).thenReturn(null);

            Result<?> result = userService.getUserInfo(999L);

            assertEquals(ResultCode.USER_NOT_FOUND, result.getCode());
        }
    }

    @Nested
    @DisplayName("updateUserInfo() 更新用户信息")
    class UpdateUserInfoTests {

        @Test
        @DisplayName("更新昵称成功")
        void updateUserInfo_Success() {
            User user = createTestUser(200L, RoleEnum.NORMAL_USER.getCode(), UserStatus.NORMAL.getCode());
            when(userMapper.selectById(200L)).thenReturn(user);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            Result<?> result = userService.updateUserInfo(200L, "新昵称");

            assertEquals(ResultCode.SUCCESS, result.getCode());
            verify(userMapper).updateById(any(User.class));
        }

        @Test
        @DisplayName("更新用户信息失败 - 用户不存在")
        void updateUserInfo_UserNotFound() {
            when(userMapper.selectById(999L)).thenReturn(null);

            Result<?> result = userService.updateUserInfo(999L, "新昵称");

            assertEquals(ResultCode.USER_NOT_FOUND, result.getCode());
        }
    }

    @Nested
    @DisplayName("getInviteCode() 获取邀请码")
    class GetInviteCodeTests {

        @Test
        @DisplayName("获取邀请码成功 - 已有邀请码")
        void getInviteCode_Success_HasCode() {
            InviteCode code = createTestInviteCode(100L, RoleEnum.NORMAL_USER.getCode(), InviteCodeStatus.ACTIVE.getCode());
            when(inviteCodeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(code);

            Result<?> result = userService.getInviteCode(100L);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertEquals("TESTCODE", result.getData());
        }

        @Test
        @DisplayName("获取邀请码成功 - 无邀请码")
        void getInviteCode_Success_NoCode() {
            when(inviteCodeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            Result<?> result = userService.getInviteCode(100L);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNull(result.getData());
        }
    }

    @Nested
    @DisplayName("createInviteCode() 创建邀请码")
    class CreateInviteCodeTests {

        @Test
        @DisplayName("创建邀请码成功 - 新建")
        void createInviteCode_Success_New() {
            when(inviteCodeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(inviteCodeMapper.insert(any(InviteCode.class))).thenReturn(1);

            Result<?> result = userService.createInviteCode(100L);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNotNull(result.getData());
            verify(inviteCodeMapper).insert(any(InviteCode.class));
        }

        @Test
        @DisplayName("创建邀请码成功 - 替换旧码")
        void createInviteCode_Success_ReplaceOld() {
            InviteCode oldCode = createTestInviteCode(100L, RoleEnum.NORMAL_USER.getCode(), InviteCodeStatus.ACTIVE.getCode());
            when(inviteCodeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(oldCode);
            when(inviteCodeMapper.updateById(any(InviteCode.class))).thenReturn(1);
            when(inviteCodeMapper.insert(any(InviteCode.class))).thenReturn(1);

            Result<?> result = userService.createInviteCode(100L);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNotNull(result.getData());
            verify(inviteCodeMapper).updateById(oldCode);
            verify(inviteCodeMapper).insert(any(InviteCode.class));
        }
    }

    @Nested
    @DisplayName("createUser() 创建用户")
    class CreateUserTests {

        @Test
        @DisplayName("创建用户成功")
        void createUser_Success() {
            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(userMapper.insert(any(User.class))).thenReturn(1);

            Result<?> result = userService.createUser(100L, "newuser", "新用户", "password123");

            assertEquals(ResultCode.SUCCESS, result.getCode());
            verify(userMapper).insert(any(User.class));
        }

        @Test
        @DisplayName("创建用户失败 - 用户名已存在")
        void createUser_Fail_UsernameExists() {
            User existing = createTestUser(2L, RoleEnum.NORMAL_USER.getCode(), UserStatus.NORMAL.getCode());
            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

            Result<?> result = userService.createUser(100L, "existinguser", "已有用户", "password123");

            assertEquals(ResultCode.USERNAME_EXISTS, result.getCode());
            assertEquals("用户名已存在", result.getMessage());
            verify(userMapper, never()).insert(any(User.class));
        }
    }

    @Nested
    @DisplayName("resetUserPassword() 重置密码")
    class ResetUserPasswordTests {

        @Test
        @DisplayName("重置密码成功")
        void resetUserPassword_Success() {
            User user = createTestUser(200L, RoleEnum.NORMAL_USER.getCode(), UserStatus.NORMAL.getCode());
            user.setParentId(100L);

            when(userMapper.selectById(200L)).thenReturn(user);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            Result<?> result = userService.resetUserPassword(100L, 200L);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNotNull(result.getData());
            verify(userMapper).updateById(any(User.class));
        }

        @Test
        @DisplayName("重置密码失败 - 用户不存在")
        void resetUserPassword_Fail_UserNotFound() {
            when(userMapper.selectById(999L)).thenReturn(null);

            Result<?> result = userService.resetUserPassword(100L, 999L);

            assertEquals(ResultCode.USER_NOT_FOUND, result.getCode());
        }

        @Test
        @DisplayName("重置密码失败 - 无权重置该用户")
        void resetUserPassword_Fail_NoPermission() {
            User user = createTestUser(200L, RoleEnum.NORMAL_USER.getCode(), UserStatus.NORMAL.getCode());
            user.setParentId(999L);

            when(userMapper.selectById(200L)).thenReturn(user);

            Result<?> result = userService.resetUserPassword(100L, 200L);

            assertEquals(ResultCode.FAIL, result.getCode());
            assertEquals("无权重置该用户密码", result.getMessage());
        }
    }
}