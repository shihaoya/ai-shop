package com.sh.aishop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sh.aishop.common.Result;
import com.sh.aishop.common.ResultCode;
import com.sh.aishop.dto.UserDTO;
import com.sh.aishop.entity.User;
import com.sh.aishop.entity.Points;
import com.sh.aishop.mapper.UserMapper;
import com.sh.aishop.mapper.PointsMapper;
import com.sh.aishop.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PointsMapper pointsMapper;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取当前用户信息
     */
    public Result<?> getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail(ResultCode.USER_NOT_FOUND, "用户不存在");
        }

        UserDTO dto = new UserDTO();
        dto.setId(user.getId().toString());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().format(DTF) : null);

        // 获取积分余额
        Points latest = pointsMapper.selectOne(new LambdaQueryWrapper<Points>()
                .eq(Points::getUserId, userId)
                .orderByDesc(Points::getCreatedAt).last("LIMIT 1"));
        dto.setPointsBalance(latest != null ? latest.getBalance().toString() : "0");

        return Result.success(dto);
    }

    /**
     * 更新用户昵称
     */
    public Result<?> updateUserInfo(Long userId, String nickname) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
        user.setNickname(nickname);
        userMapper.updateById(user);
        return Result.success();
    }

    /**
     * 修改密码
     */
    @Transactional
    public Result<?> changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail(ResultCode.USER_NOT_FOUND, "用户不存在");
        }

        // 验证旧密码
        if (!SecurityUtil.matchPassword(oldPassword, user.getPassword())) {
            return Result.fail(ResultCode.PASSWORD_ERROR, "旧密码错误");
        }

        // 更新密码
        user.setPassword(SecurityUtil.encryptPassword(newPassword));
        userMapper.updateById(user);
        return Result.success();
    }
}