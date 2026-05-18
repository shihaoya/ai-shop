package com.sh.aishop.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sh.aishop.common.Result;
import com.sh.aishop.common.ResultCode;
import com.sh.aishop.auth.dto.*;
import com.sh.aishop.common.entity.*;
import com.sh.aishop.common.enums.*;
import com.sh.aishop.auth.mapper.InviteCodeMapper;
import com.sh.aishop.auth.mapper.UserMapper;
import com.sh.aishop.user.mapper.PointsMapper;
import com.sh.aishop.util.JwtUtil;
import com.sh.aishop.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class AuthService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private InviteCodeMapper inviteCodeMapper;
    @Autowired
    private PointsMapper pointsMapper;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private StringRedisTemplate redisTemplate;

    public Result<?> login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .eq(User::getDeleted, 0));

        if (user == null) {
            return Result.fail(ResultCode.USERNAME_PASSWORD_ERROR, "用户名或密码错误");
        }

        if (!SecurityUtil.matchPassword(request.getPassword(), user.getPassword())) {
            return Result.fail(ResultCode.USERNAME_PASSWORD_ERROR, "用户名或密码错误");
        }

        if (user.getStatus() == UserStatus.PENDING.getCode()) {
            return Result.fail(ResultCode.USER_DISABLED, "账号待审核");
        }
        if (user.getStatus() == UserStatus.FROZEN.getCode()) {
            return Result.fail(ResultCode.USER_DISABLED, "账号已冻结");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        UserDTO dto = toUserDTO(user);
        dto.setPointsBalance("0");

        return Result.success(java.util.Map.of("token", token, "userinfo", dto));
    }

    @Transactional
    public Result<?> register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return Result.fail(ResultCode.PASSWORD_MISMATCH, "两次密码输入不一致");
        }

        InviteCode inviteCode = inviteCodeMapper.selectOne(new LambdaQueryWrapper<InviteCode>()
                .eq(InviteCode::getCode, request.getInviteCode())
                .eq(InviteCode::getStatus, InviteCodeStatus.ACTIVE.getCode())
                .eq(InviteCode::getDeleted, 0));

        if (inviteCode == null) {
            return Result.fail(ResultCode.INVITE_CODE_INVALID, "邀请码无效");
        }

        User existUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .eq(User::getDeleted, 0));
        if (existUser != null) {
            return Result.fail(ResultCode.USERNAME_EXISTS, "用户名已存在");
        }

        User creator = userMapper.selectById(inviteCode.getCreatorId());
        int role = inviteCode.getRole();
        long parentId = inviteCode.getCreatorId();

        User user = new User();
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname());
        user.setPassword(SecurityUtil.encryptPassword(request.getPassword()));
        user.setRole(role);
        user.setParentId(parentId);
        user.setStatus(UserStatus.PENDING.getCode());
        userMapper.insert(user);

        return Result.success(java.util.Collections.singletonMap("userId", user.getId().toString()));
    }

    public Result<?> getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail(ResultCode.USER_NOT_FOUND, "用户不存在");
        }

        UserDTO dto = toUserDTO(user);

        Points latestPoints = pointsMapper.selectOne(new LambdaQueryWrapper<Points>()
                .eq(Points::getUserId, userId)
                .eq(Points::getDeleted, 0)
                .orderByDesc(Points::getCreatedAt)
                .last("LIMIT 1"));
        dto.setPointsBalance(latestPoints != null ? latestPoints.getBalance().toString() : "0");

        return Result.success(dto);
    }

    @Transactional
    public Result<?> changePassword(Long userId, PasswordRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail(ResultCode.USER_NOT_FOUND, "用户不存在");
        }

        if (!SecurityUtil.matchPassword(request.getOldPassword(), user.getPassword())) {
            return Result.fail(ResultCode.PASSWORD_ERROR, "旧密码错误");
        }

        user.setPassword(SecurityUtil.encryptPassword(request.getNewPassword()));
        userMapper.updateById(user);

        return Result.success();
    }

    public Result<?> logout(String token) {
        redisTemplate.opsForValue().set("blacklist:" + token, "1",
                java.time.Duration.ofMillis(604800000));
        return Result.success();
    }

    @Transactional
    public Result<?> updateUserInfo(Long userId, String nickname) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail(ResultCode.USER_NOT_FOUND, "用户不存在");
        }

        if (StringUtils.hasText(nickname)) {
            user.setNickname(nickname);
        }

        userMapper.updateById(user);
        return Result.success(toUserDTO(user));
    }

    private UserDTO toUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId().toString());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        return dto;
    }
}