package com.sh.aishop.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sh.aishop.common.Result;
import com.sh.aishop.common.ResultCode;
import com.sh.aishop.common.dto.UserDTO;
import com.sh.aishop.auth.dto.*;
import com.sh.aishop.common.entity.*;
import com.sh.aishop.common.enums.*;
import com.sh.aishop.auth.mapper.InviteCodeMapper;
import com.sh.aishop.auth.mapper.UserMapper;
import com.sh.aishop.user.mapper.PointsMapper;
import com.sh.aishop.util.JwtUtil;
import com.sh.aishop.util.SecurityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 单元测试")
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private InviteCodeMapper inviteCodeMapper;

    @Mock
    private PointsMapper pointsMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private com.sh.aishop.auth.service.AuthService authService;

    private static final String PLAIN_PASSWORD = "password123";
    private static String ENCODED_PASSWORD;

    static {
        ENCODED_PASSWORD = SecurityUtil.encryptPassword(PLAIN_PASSWORD);
    }

    private User createTestUser(Long id, String username, Integer status, Integer role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setNickname("测试用户");
        user.setPassword(ENCODED_PASSWORD);
        user.setStatus(status);
        user.setRole(role);
        user.setDeleted(0);
        return user;
    }

    private InviteCode createTestInviteCode(String code, Long creatorId, Integer role, Integer status) {
        InviteCode inviteCode = new InviteCode();
        inviteCode.setCode(code);
        inviteCode.setCreatorId(creatorId);
        inviteCode.setRole(role);
        inviteCode.setStatus(status);
        inviteCode.setDeleted(0);
        return inviteCode;
    }

    @Nested
    @DisplayName("login() 登录测试")
    class LoginTests {

        @Test
        @DisplayName("登录成功 - 返回token和用户信息")
        void login_Success() {
            User user = createTestUser(1L, "testuser", UserStatus.NORMAL.getCode(), RoleEnum.NORMAL_USER.getCode());
            LoginRequest request = new LoginRequest();
            request.setUsername("testuser");
            request.setPassword(PLAIN_PASSWORD);

            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
            when(jwtUtil.generateToken(eq(1L), eq("testuser"), eq(RoleEnum.NORMAL_USER.getCode())))
                    .thenReturn("mock-jwt-token");

            Result<?> result = authService.login(request);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNotNull(result.getData());
            assertTrue(result.getData() instanceof java.util.Map);
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> data = (java.util.Map<String, Object>) result.getData();
            assertEquals("mock-jwt-token", data.get("token"));
            assertNotNull(data.get("userinfo"));
        }

        @Test
        @DisplayName("登录失败 - 用户不存在")
        void login_UserNotFound() {
            LoginRequest request = new LoginRequest();
            request.setUsername("nonexistent");
            request.setPassword(PLAIN_PASSWORD);

            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            Result<?> result = authService.login(request);

            assertEquals(ResultCode.USERNAME_PASSWORD_ERROR, result.getCode());
            assertEquals("用户名或密码错误", result.getMessage());
        }

        @Test
        @DisplayName("登录失败 - 密码错误")
        void login_WrongPassword() {
            User user = createTestUser(1L, "testuser", UserStatus.NORMAL.getCode(), RoleEnum.NORMAL_USER.getCode());
            LoginRequest request = new LoginRequest();
            request.setUsername("testuser");
            request.setPassword("wrongpassword");

            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

            Result<?> result = authService.login(request);

            assertEquals(ResultCode.USERNAME_PASSWORD_ERROR, result.getCode());
            assertEquals("用户名或密码错误", result.getMessage());
        }

        @Test
        @DisplayName("登录失败 - 用户待审核")
        void login_UserPending() {
            User user = createTestUser(1L, "testuser", UserStatus.PENDING.getCode(), RoleEnum.NORMAL_USER.getCode());
            LoginRequest request = new LoginRequest();
            request.setUsername("testuser");
            request.setPassword(PLAIN_PASSWORD);

            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

            Result<?> result = authService.login(request);

            assertEquals(ResultCode.USER_DISABLED, result.getCode());
            assertEquals("账号待审核", result.getMessage());
        }

        @Test
        @DisplayName("登录失败 - 用户已冻结")
        void login_UserFrozen() {
            User user = createTestUser(1L, "testuser", UserStatus.FROZEN.getCode(), RoleEnum.NORMAL_USER.getCode());
            LoginRequest request = new LoginRequest();
            request.setUsername("testuser");
            request.setPassword(PLAIN_PASSWORD);

            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

            Result<?> result = authService.login(request);

            assertEquals(ResultCode.USER_DISABLED, result.getCode());
            assertEquals("账号已冻结", result.getMessage());
        }
    }

    @Nested
    @DisplayName("register() 注册测试")
    class RegisterTests {

        @Test
        @DisplayName("注册成功")
        void register_Success() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser");
            request.setNickname("新用户");
            request.setPassword(PLAIN_PASSWORD);
            request.setConfirmPassword(PLAIN_PASSWORD);
            request.setInviteCode("VALIDCODE");

            InviteCode inviteCode = createTestInviteCode("VALIDCODE", 100L, RoleEnum.SHOP_USER.getCode(), InviteCodeStatus.ACTIVE.getCode());
            User creator = createTestUser(100L, "creator", UserStatus.NORMAL.getCode(), RoleEnum.ADMIN.getCode());

            when(inviteCodeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(inviteCode);
            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(userMapper.selectById(100L)).thenReturn(creator);
            when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
                User u = invocation.getArgument(0);
                u.setId(999L);
                return 1;
            });

            Result<?> result = authService.register(request);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNotNull(result.getData());
            assertTrue(result.getData() instanceof java.util.Map);
            @SuppressWarnings("unchecked")
            java.util.Map<String, String> data = (java.util.Map<String, String>) result.getData();
            assertNotNull(data.get("userId"));
            assertEquals("999", data.get("userId"));
        }

        @Test
        @DisplayName("注册失败 - 两次密码输入不一致")
        void register_PasswordMismatch() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser");
            request.setNickname("新用户");
            request.setPassword(PLAIN_PASSWORD);
            request.setConfirmPassword("differentpassword");
            request.setInviteCode("VALIDCODE");

            Result<?> result = authService.register(request);

            assertEquals(ResultCode.PASSWORD_MISMATCH, result.getCode());
            assertEquals("两次密码输入不一致", result.getMessage());
            verify(inviteCodeMapper, never()).selectOne(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("注册失败 - 无效邀请码")
        void register_InvalidInviteCode() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser");
            request.setNickname("新用户");
            request.setPassword(PLAIN_PASSWORD);
            request.setConfirmPassword(PLAIN_PASSWORD);
            request.setInviteCode("INVALIDCODE");

            when(inviteCodeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            Result<?> result = authService.register(request);

            assertEquals(ResultCode.INVITE_CODE_INVALID, result.getCode());
            assertEquals("邀请码无效", result.getMessage());
        }

        @Test
        @DisplayName("注册失败 - 用户名已存在")
        void register_UsernameExists() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("existinguser");
            request.setNickname("新用户");
            request.setPassword(PLAIN_PASSWORD);
            request.setConfirmPassword(PLAIN_PASSWORD);
            request.setInviteCode("VALIDCODE");

            InviteCode inviteCode = createTestInviteCode("VALIDCODE", 100L, RoleEnum.SHOP_USER.getCode(), InviteCodeStatus.ACTIVE.getCode());
            User existingUser = createTestUser(2L, "existinguser", UserStatus.NORMAL.getCode(), RoleEnum.NORMAL_USER.getCode());

            when(inviteCodeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(inviteCode);
            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingUser);

            Result<?> result = authService.register(request);

            assertEquals(ResultCode.USERNAME_EXISTS, result.getCode());
            assertEquals("用户名已存在", result.getMessage());
        }
    }

    @Nested
    @DisplayName("getUserInfo() 获取用户信息测试")
    class GetUserInfoTests {

        @Test
        @DisplayName("获取用户信息成功 - 有积分记录")
        void getUserInfo_Success_WithPoints() {
            User user = createTestUser(1L, "testuser", UserStatus.NORMAL.getCode(), RoleEnum.NORMAL_USER.getCode());
            Points points = new Points();
            points.setUserId(1L);
            points.setBalance(500);

            when(userMapper.selectById(1L)).thenReturn(user);
            when(pointsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(points);

            Result<?> result = authService.getUserInfo(1L);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNotNull(result.getData());
            assertTrue(result.getData() instanceof UserDTO);
            UserDTO dto = (UserDTO) result.getData();
            assertEquals("500", dto.getPointsBalance());
        }

        @Test
        @DisplayName("获取用户信息成功 - 无积分记录")
        void getUserInfo_Success_NoPoints() {
            User user = createTestUser(1L, "testuser", UserStatus.NORMAL.getCode(), RoleEnum.NORMAL_USER.getCode());

            when(userMapper.selectById(1L)).thenReturn(user);
            when(pointsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            Result<?> result = authService.getUserInfo(1L);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNotNull(result.getData());
            assertTrue(result.getData() instanceof UserDTO);
            UserDTO dto = (UserDTO) result.getData();
            assertEquals("0", dto.getPointsBalance());
        }

        @Test
        @DisplayName("获取用户信息失败 - 用户不存在")
        void getUserInfo_UserNotFound() {
            when(userMapper.selectById(999L)).thenReturn(null);

            Result<?> result = authService.getUserInfo(999L);

            assertEquals(ResultCode.USER_NOT_FOUND, result.getCode());
            assertEquals("用户不存在", result.getMessage());
        }
    }

    @Nested
    @DisplayName("changePassword() 修改密码测试")
    class ChangePasswordTests {

        @Test
        @DisplayName("修改密码成功")
        void changePassword_Success() {
            User user = createTestUser(1L, "testuser", UserStatus.NORMAL.getCode(), RoleEnum.NORMAL_USER.getCode());

            PasswordRequest request = new PasswordRequest();
            request.setOldPassword(PLAIN_PASSWORD);
            request.setNewPassword("newpassword456");

            when(userMapper.selectById(1L)).thenReturn(user);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            Result<?> result = authService.changePassword(1L, request);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            verify(userMapper).updateById(any(User.class));
        }

        @Test
        @DisplayName("修改密码失败 - 旧密码错误")
        void changePassword_WrongOldPassword() {
            User user = createTestUser(1L, "testuser", UserStatus.NORMAL.getCode(), RoleEnum.NORMAL_USER.getCode());

            PasswordRequest request = new PasswordRequest();
            request.setOldPassword("wrongoldpassword");
            request.setNewPassword("newpassword456");

            when(userMapper.selectById(1L)).thenReturn(user);

            Result<?> result = authService.changePassword(1L, request);

            assertEquals(ResultCode.PASSWORD_ERROR, result.getCode());
            assertEquals("旧密码错误", result.getMessage());
            verify(userMapper, never()).updateById(any(User.class));
        }

        @Test
        @DisplayName("修改密码失败 - 用户不存在")
        void changePassword_UserNotFound() {
            PasswordRequest request = new PasswordRequest();
            request.setOldPassword(PLAIN_PASSWORD);
            request.setNewPassword("newpassword");

            when(userMapper.selectById(999L)).thenReturn(null);

            Result<?> result = authService.changePassword(999L, request);

            assertEquals(ResultCode.USER_NOT_FOUND, result.getCode());
            assertEquals("用户不存在", result.getMessage());
        }
    }

    @Nested
    @DisplayName("logout() 登出测试")
    class LogoutTests {

        @Test
        @DisplayName("登出成功")
        void logout_Success() {
            String token = "valid-jwt-token";

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            doNothing().when(valueOperations).set(anyString(), anyString(), any(java.time.Duration.class));

            Result<?> result = authService.logout(token);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            verify(redisTemplate).opsForValue();
            verify(valueOperations).set(eq("blacklist:" + token), eq("1"), any(java.time.Duration.class));
        }
    }
}