package com.sh.aishop.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sh.aishop.common.Result;
import com.sh.aishop.common.ResultCode;
import com.sh.aishop.common.entity.Message;
import com.sh.aishop.common.entity.Points;
import com.sh.aishop.common.entity.User;
import com.sh.aishop.common.enums.MessageType;
import com.sh.aishop.common.enums.PointsType;
import com.sh.aishop.common.enums.RoleEnum;
import com.sh.aishop.common.enums.UserStatus;
import com.sh.aishop.common.dto.PageRequest;
import com.sh.aishop.message.service.MessageService;
import com.sh.aishop.shop.service.ShopService;
import com.sh.aishop.user.mapper.PointsMapper;
import com.sh.aishop.user.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PointsService 单元测试")
class PointsServiceTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private PointsMapper pointsMapper;
    @Mock
    private ShopService shopService;
    @Mock
    private MessageService messageService;

    @InjectMocks
    private com.sh.aishop.user.service.PointsService pointsService;

    private User createTestUser(Long id, Long parentId, Integer status) {
        User user = new User();
        user.setId(id);
        user.setUsername("testuser");
        user.setNickname("测试用户");
        user.setParentId(parentId);
        user.setRole(RoleEnum.NORMAL_USER.getCode());
        user.setStatus(status);
        user.setDeleted(0);
        return user;
    }

    private Points createTestPoints(Long userId, Integer balance, Integer type) {
        Points points = new Points();
        points.setId(1L);
        points.setUserId(userId);
        points.setAmount(100);
        points.setBalance(balance);
        points.setType(type);
        points.setDeleted(0);
        return points;
    }

    private Map<String, Object> shopDataWithId(Long shopId) {
        Map<String, Object> data = new HashMap<>();
        data.put("hasShop", true);
        data.put("id", shopId.toString());
        return data;
    }

    @Nested
    @DisplayName("getUsers() 获取用户列表")
    class GetUsersTests {

        @Test
        @DisplayName("获取用户列表成功")
        void getUsers_Success() {
            User user = createTestUser(200L, 100L, UserStatus.NORMAL.getCode());

            Result<?> shopResult = Result.success(shopDataWithId(100L));
            doReturn(shopResult).when(shopService).getMyShop(any());
            when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(user));
            when(pointsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            PageRequest pageRequest = new PageRequest();
            pageRequest.setPage(1);
            pageRequest.setPageSize(10);

            Result<?> result = pointsService.getUsers(100L, pageRequest);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNotNull(result.getData());
        }

        @Test
        @DisplayName("获取用户列表失败 - 店铺不存在")
        void getUsers_Fail_NoShop() {
            Result<?> shopResult = Result.success(Collections.singletonMap("hasShop", false));
            doReturn(shopResult).when(shopService).getMyShop(any());

            PageRequest pageRequest = new PageRequest();
            pageRequest.setPage(1);
            pageRequest.setPageSize(10);

            Result<?> result = pointsService.getUsers(100L, pageRequest);

            assertEquals(ResultCode.SHOP_NOT_FOUND, result.getCode());
        }
    }

    @Nested
    @DisplayName("adjustPoints() 调整积分")
    class AdjustPointsTests {

        @Test
        @DisplayName("发放积分成功")
        void adjustPoints_Success_Grant() {
            User user = createTestUser(200L, 100L, UserStatus.NORMAL.getCode());
            Points latest = createTestPoints(200L, 100, PointsType.GRANT.getCode());

            Result<?> shopResult = Result.success(shopDataWithId(100L));
            doReturn(shopResult).when(shopService).getMyShop(any());
            when(userMapper.selectById(200L)).thenReturn(user);
            when(pointsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(latest);
            when(pointsMapper.insert(any(Points.class))).thenReturn(1);
            doNothing().when(messageService).insertMessage(any(Message.class));

            Result<?> result = pointsService.adjustPoints(100L, 200L, 50, "测试发放");

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertEquals(150, result.getData());
        }

        @Test
        @DisplayName("扣除积分成功")
        void adjustPoints_Success_Deduct() {
            User user = createTestUser(200L, 100L, UserStatus.NORMAL.getCode());
            Points latest = createTestPoints(200L, 100, PointsType.GRANT.getCode());

            Result<?> shopResult = Result.success(shopDataWithId(100L));
            doReturn(shopResult).when(shopService).getMyShop(any());
            when(userMapper.selectById(200L)).thenReturn(user);
            when(pointsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(latest);
            when(pointsMapper.insert(any(Points.class))).thenReturn(1);
            doNothing().when(messageService).insertMessage(any(Message.class));

            Result<?> result = pointsService.adjustPoints(100L, 200L, -30, "测试扣除");

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertEquals(70, result.getData());
        }

        @Test
        @DisplayName("调整积分失败 - 积分不足")
        void adjustPoints_Fail_InsufficientPoints() {
            User user = createTestUser(200L, 100L, UserStatus.NORMAL.getCode());
            Points latest = createTestPoints(200L, 20, PointsType.GRANT.getCode());

            Result<?> shopResult = Result.success(shopDataWithId(100L));
            doReturn(shopResult).when(shopService).getMyShop(any());
            when(userMapper.selectById(200L)).thenReturn(user);
            when(pointsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(latest);

            Result<?> result = pointsService.adjustPoints(100L, 200L, -50, "扣除");

            assertEquals(ResultCode.FAIL, result.getCode());
            assertEquals("积分不足", result.getMessage());
        }

        @Test
        @DisplayName("调整积分失败 - 用户不存在")
        void adjustPoints_Fail_UserNotFound() {
            Result<?> shopResult = Result.success(shopDataWithId(100L));
            doReturn(shopResult).when(shopService).getMyShop(any());
            when(userMapper.selectById(999L)).thenReturn(null);

            Result<?> result = pointsService.adjustPoints(100L, 999L, 50, "发放");

            assertEquals(ResultCode.USER_NOT_FOUND, result.getCode());
        }
    }

    @Nested
    @DisplayName("getPoints() 获取积分")
    class GetPointsTests {

        @Test
        @DisplayName("获取积分成功 - 有记录")
        void getPoints_Success_WithRecord() {
            Points points = createTestPoints(200L, 500, PointsType.GRANT.getCode());
            when(pointsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(points);

            Result<?> result = pointsService.getPoints(200L);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNotNull(result.getData());
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> data = (java.util.Map<String, Object>) result.getData();
            assertEquals(500, data.get("points"));
        }

        @Test
        @DisplayName("获取积分成功 - 无记录")
        void getPoints_Success_NoRecord() {
            when(pointsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            Result<?> result = pointsService.getPoints(200L);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNotNull(result.getData());
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> data = (java.util.Map<String, Object>) result.getData();
            assertEquals(0, data.get("points"));
        }
    }

    @Nested
    @DisplayName("approveUser() 审核通过")
    class ApproveUserTests {

        @Test
        @DisplayName("审核通过成功")
        void approveUser_Success() {
            User user = createTestUser(200L, 100L, UserStatus.PENDING.getCode());

            Result<?> shopResult = Result.success(shopDataWithId(100L));
            doReturn(shopResult).when(shopService).getMyShop(any());
            when(userMapper.selectById(200L)).thenReturn(user);
            when(userMapper.updateById(any(User.class))).thenReturn(1);
            doNothing().when(messageService).insertMessage(any(Message.class));

            Result<?> result = pointsService.approveUser(100L, 200L);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            verify(userMapper).updateById(any(User.class));
        }

        @Test
        @DisplayName("审核通过失败 - 用户不是待审核状态")
        void approveUser_Fail_NotPending() {
            User user = createTestUser(200L, 100L, UserStatus.NORMAL.getCode());

            Result<?> shopResult = Result.success(shopDataWithId(100L));
            doReturn(shopResult).when(shopService).getMyShop(any());
            when(userMapper.selectById(200L)).thenReturn(user);

            Result<?> result = pointsService.approveUser(100L, 200L);

            assertEquals(ResultCode.FAIL, result.getCode());
            assertEquals("用户不是待审核状态", result.getMessage());
        }
    }

    @Nested
    @DisplayName("rejectUser() 审核拒绝")
    class RejectUserTests {

        @Test
        @DisplayName("审核拒绝成功")
        void rejectUser_Success() {
            User user = createTestUser(200L, 100L, UserStatus.PENDING.getCode());

            Result<?> shopResult = Result.success(shopDataWithId(100L));
            doReturn(shopResult).when(shopService).getMyShop(any());
            when(userMapper.selectById(200L)).thenReturn(user);
            when(userMapper.updateById(any(User.class))).thenReturn(1);
            doNothing().when(messageService).insertMessage(any(Message.class));

            Result<?> result = pointsService.rejectUser(100L, 200L);

            assertEquals(ResultCode.SUCCESS, result.getCode());
        }

        @Test
        @DisplayName("审核拒绝失败 - 用户不是待审核状态")
        void rejectUser_Fail_NotPending() {
            User user = createTestUser(200L, 100L, UserStatus.NORMAL.getCode());

            Result<?> shopResult = Result.success(shopDataWithId(100L));
            doReturn(shopResult).when(shopService).getMyShop(any());
            when(userMapper.selectById(200L)).thenReturn(user);

            Result<?> result = pointsService.rejectUser(100L, 200L);

            assertEquals(ResultCode.FAIL, result.getCode());
            assertEquals("用户不是待审核状态", result.getMessage());
        }
    }
}