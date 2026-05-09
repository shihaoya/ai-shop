package com.sh.aishop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sh.aishop.common.Result;
import com.sh.aishop.common.ResultCode;
import com.sh.aishop.dto.*;
import com.sh.aishop.entity.*;
import com.sh.aishop.entity.enums.*;
import com.sh.aishop.mapper.*;
import com.sh.aishop.util.SnowflakeIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ShopMapper shopMapper;
    @Autowired
    private InviteCodeMapper inviteCodeMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    // ============ 店铺管理 ============
    public Result<?> getShops(PageRequest pageRequest) {
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Shop::getDeleted, 0);
        if (StringUtils.hasText(pageRequest.getKeyword())) {
            wrapper.like(Shop::getName, pageRequest.getKeyword());
        }
        wrapper.orderByDesc(Shop::getCreatedAt);

        List<Shop> shops = shopMapper.selectList(wrapper);
        Long total = (long) shops.size();

        // 分页
        int offset = pageRequest.getOffset().intValue();
        int pageSize = pageRequest.getPageSize();
        shops = shops.stream().skip(offset).limit(pageSize).collect(Collectors.toList());

        // 转换为DTO
        List<ShopDTO> dtos = new ArrayList<>();
        for (Shop shop : shops) {
            ShopDTO dto = new ShopDTO();
            dto.setId(shop.getId().toString());
            dto.setOperatorId(shop.getOperatorId().toString());
            User operator = userMapper.selectById(shop.getOperatorId());
            dto.setOperatorName(operator != null ? operator.getNickname() : "");
            dto.setName(shop.getName());
            dto.setDescription(shop.getDescription());
            dto.setStatus(shop.getStatus());
            dto.setIsActive(shop.getIsActive());
            dtos.add(dto);
        }

        return Result.success(new PageResult<>(dtos, total, pageRequest.getPage(), pageRequest.getPageSize()));
    }

    @Transactional
    public Result<?> auditShop(Long shopId, Integer status) {
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在");
        }

        shop.setStatus(status);
        shopMapper.updateById(shop);
        return Result.success();
    }

    // ============ 用户管理 ============
    public Result<?> getUsers(PageRequest pageRequest) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0);
        // 排除管理员
        wrapper.ne(User::getRole, RoleEnum.ADMIN.getCode());
        if (StringUtils.hasText(pageRequest.getKeyword())) {
            wrapper.and(w -> w.like(User::getUsername, pageRequest.getKeyword())
                    .or().like(User::getNickname, pageRequest.getKeyword()));
        }
        wrapper.orderByDesc(User::getCreatedAt);

        List<User> users = userMapper.selectList(wrapper);
        Long total = (long) users.size();

        int offset = pageRequest.getOffset().intValue();
        int pageSize = pageRequest.getPageSize();
        users = users.stream().skip(offset).limit(pageSize).collect(Collectors.toList());

        List<UserDTO> dtos = new ArrayList<>();
        for (User user : users) {
            UserDTO dto = new UserDTO();
            dto.setId(user.getId().toString());
            dto.setUsername(user.getUsername());
            dto.setNickname(user.getNickname());
            dto.setRole(user.getRole());
            dto.setStatus(user.getStatus());
            dtos.add(dto);
        }

        return Result.success(new PageResult<>(dtos, total, pageRequest.getPage(), pageRequest.getPageSize()));
    }

    @Transactional
    public Result<?> changeUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
        if (user.getRole() == RoleEnum.ADMIN.getCode()) {
            return Result.fail(ResultCode.FAIL, "不能操作管理员账号");
        }

        user.setStatus(status);
        userMapper.updateById(user);
        return Result.success();
    }

    @Transactional
    public Result<?> approveUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
        if (user.getStatus() != UserStatus.PENDING.getCode()) {
            return Result.fail(ResultCode.FAIL, "用户不是待审核状态");
        }

        user.setStatus(UserStatus.NORMAL.getCode());
        userMapper.updateById(user);
        return Result.success();
    }

    // ============ 邀请码管理 ============
    public Result<?> getInviteCode(Long adminId) {
        // 查找当前有效的邀请码
        InviteCode code = inviteCodeMapper.selectOne(new LambdaQueryWrapper<InviteCode>()
                .eq(InviteCode::getCreatorId, adminId)
                .eq(InviteCode::getRole, RoleEnum.ADMIN.getCode())
                .eq(InviteCode::getStatus, InviteCodeStatus.ACTIVE.getCode())
                .eq(InviteCode::getDeleted, 0));

        return Result.success(code != null ? code.getCode() : null);
    }

    @Transactional
    public Result<?> createInviteCode(Long adminId) {
        // 作废旧的邀请码
        InviteCode oldCode = inviteCodeMapper.selectOne(new LambdaQueryWrapper<InviteCode>()
                .eq(InviteCode::getCreatorId, adminId)
                .eq(InviteCode::getRole, RoleEnum.ADMIN.getCode())
                .eq(InviteCode::getStatus, InviteCodeStatus.ACTIVE.getCode())
                .eq(InviteCode::getDeleted, 0));

        if (oldCode != null) {
            oldCode.setStatus(InviteCodeStatus.INVALID.getCode());
            inviteCodeMapper.updateById(oldCode);
        }

        // 创建新的邀请码
        InviteCode newCode = new InviteCode();
        newCode.setId(SnowflakeIdUtil.nextId());
        newCode.setCode(generateCode());
        newCode.setRole(RoleEnum.ADMIN.getCode());
        newCode.setCreatorId(adminId);
        newCode.setStatus(InviteCodeStatus.ACTIVE.getCode());
        inviteCodeMapper.insert(newCode);

        return Result.success(newCode.getCode());
    }

    private String generateCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}