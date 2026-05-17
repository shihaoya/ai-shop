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
import com.sh.aishop.util.SecurityUtil;
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
    private PointsMapper pointsMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    // ============ 店铺管理 ============
    public Result<?> getShops(PageRequest pageRequest) {
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Shop::getDeleted, 0);
        // 状态筛选
        if (pageRequest.getStatus() != null) {
            wrapper.eq(Shop::getStatus, pageRequest.getStatus());
        }
        // 关键词搜索
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
            dto.setRejectReason(shop.getRejectReason());
            dtos.add(dto);
        }

        return Result.success(new PageResult<>(dtos, total, pageRequest.getPage(), pageRequest.getPageSize()));
    }

    @Transactional
    public Result<?> auditShop(String shopId, Integer statusCode, String rejectReason) {
        Shop shop = shopMapper.selectById(Long.parseLong(shopId));
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在");
        }

        ShopStatus status = ShopStatus.fromCode(statusCode);
        if (status == null) {
            return Result.fail(ResultCode.FAIL, "无效的审核状态");
        }
        shop.setStatus(status.getCode());
        if (status == ShopStatus.REJECTED && rejectReason != null) {
            shop.setRejectReason(rejectReason);
        }
        shopMapper.updateById(shop);
        return Result.success();
    }

    // ============ 用户管理 ============
    public Result<?> getUsers(PageRequest pageRequest) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0);
        // 排除管理员
        wrapper.ne(User::getRole, RoleEnum.ADMIN.getCode());
        // 角色筛选
        if (pageRequest.getRole() != null) {
            wrapper.eq(User::getRole, pageRequest.getRole());
        }
        // 状态筛选
        if (pageRequest.getStatus() != null) {
            wrapper.eq(User::getStatus, pageRequest.getStatus());
        }
        // 关键词搜索
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
            dto.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
            // 查询积分
            Points latest = pointsMapper.selectOne(new LambdaQueryWrapper<Points>()
                    .eq(Points::getUserId, user.getId())
                    .orderByDesc(Points::getCreatedAt)
                    .last("LIMIT 1"));
            dto.setPointsBalance(latest != null ? latest.getBalance().toString() : "0");
            dtos.add(dto);
        }

        return Result.success(new PageResult<>(dtos, total, pageRequest.getPage(), pageRequest.getPageSize()));
    }

    @Transactional
    public Result<?> changeUserStatus(String userId, Integer status) {
        User user = userMapper.selectById(Long.parseLong(userId));
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
    public Result<?> approveUser(String userId) {
        User user = userMapper.selectById(Long.parseLong(userId));
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

    @Transactional
    public Result<?> rejectUser(String userId) {
        User user = userMapper.selectById(Long.parseLong(userId));
        if (user == null) {
            return Result.fail(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
        if (user.getStatus() != UserStatus.PENDING.getCode()) {
            return Result.fail(ResultCode.FAIL, "用户不是待审核状态");
        }

        // 软删除用户（使用 UpdateWrapper 避免 @TableLogic 对 deleted 字段的干扰）
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId).set(User::getDeleted, 1);
        userMapper.update(null, updateWrapper);

        // 作废该用户的邀请码（如果该用户创建了邀请码）
        InviteCode code = inviteCodeMapper.selectOne(new LambdaQueryWrapper<InviteCode>()
                .eq(InviteCode::getCreatorId, userId)
                .eq(InviteCode::getStatus, InviteCodeStatus.ACTIVE.getCode())
                .eq(InviteCode::getDeleted, 0));
        if (code != null) {
            code.setStatus(InviteCodeStatus.INVALID.getCode());
            inviteCodeMapper.updateById(code);
        }

        return Result.success();
    }

    // ============ 邀请码管理 ============
    public Result<?> getInviteCode(Long adminId) {
        // 查找当前有效的邀请码（管理员邀请码用于创建店铺用户，role=2）
        InviteCode code = inviteCodeMapper.selectOne(new LambdaQueryWrapper<InviteCode>()
                .eq(InviteCode::getCreatorId, adminId)
                .eq(InviteCode::getRole, RoleEnum.SHOP_USER.getCode())
                .eq(InviteCode::getStatus, InviteCodeStatus.ACTIVE.getCode())
                .eq(InviteCode::getDeleted, 0));

        return Result.success(code != null ? code.getCode() : null);
    }

    @Transactional
    public Result<?> createInviteCode(Long adminId) {
        // 作废旧的邀请码
        InviteCode oldCode = inviteCodeMapper.selectOne(new LambdaQueryWrapper<InviteCode>()
                .eq(InviteCode::getCreatorId, adminId)
                .eq(InviteCode::getRole, RoleEnum.SHOP_USER.getCode())
                .eq(InviteCode::getStatus, InviteCodeStatus.ACTIVE.getCode())
                .eq(InviteCode::getDeleted, 0));

        if (oldCode != null) {
            oldCode.setStatus(InviteCodeStatus.INVALID.getCode());
            inviteCodeMapper.updateById(oldCode);
        }

        // 创建新的邀请码（管理员邀请码用于创建店铺用户，role=2）
        InviteCode newCode = new InviteCode();
        newCode.setId(SnowflakeIdUtil.nextId());
        newCode.setCode(generateCode());
        newCode.setRole(RoleEnum.SHOP_USER.getCode());
        newCode.setCreatorId(adminId);
        newCode.setStatus(InviteCodeStatus.ACTIVE.getCode());
        inviteCodeMapper.insert(newCode);

        return Result.success(newCode.getCode());
    }

    @Transactional
    public Result<?> resetUserPassword(String userId) {
        User user = userMapper.selectById(Long.parseLong(userId));
        if (user == null) {
            return Result.fail(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
        String newPassword = generateRandomPassword();
        user.setPassword(SecurityUtil.encryptPassword(newPassword));
        userMapper.updateById(user);
        return Result.success(Collections.singletonMap("password", newPassword));
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String generateCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}