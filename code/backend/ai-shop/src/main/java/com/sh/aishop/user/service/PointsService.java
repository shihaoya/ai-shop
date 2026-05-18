package com.sh.aishop.user.service;

import com.sh.aishop.common.Result;
import com.sh.aishop.common.ResultCode;
import com.sh.aishop.common.dto.UserDTO;
import com.sh.aishop.common.entity.Message;
import com.sh.aishop.common.entity.Points;
import com.sh.aishop.common.entity.User;
import com.sh.aishop.common.enums.MessageType;
import com.sh.aishop.common.enums.PointsType;
import com.sh.aishop.common.enums.RoleEnum;
import com.sh.aishop.common.enums.UserStatus;
import com.sh.aishop.dto.PageRequest;
import com.sh.aishop.dto.PageResult;
import com.sh.aishop.mapper.PointsMapper;
import com.sh.aishop.mapper.UserMapper;
import com.sh.aishop.message.service.MessageService;
import com.sh.aishop.shop.service.ShopService;
import com.sh.aishop.util.SnowflakeIdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class PointsService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PointsMapper pointsMapper;
    @Autowired
    private ShopService shopService;
    @Autowired
    private MessageService messageService;

    // ============ 店铺端用户管理 ============
    public Result<?> getUsers(Long operatorId, PageRequest pageRequest) {
        var shopResult = shopService.getMyShop(operatorId);
        if (shopResult.getData() == null || !((Map<?, ?>) shopResult.getData()).containsKey("hasShop")) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }
        Map<?, ?> shopData = (Map<?, ?>) shopResult.getData();
        if (!Boolean.TRUE.equals(shopData.get("hasShop"))) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getParentId, operatorId)
               .eq(User::getRole, RoleEnum.NORMAL_USER.getCode())
               .eq(User::getDeleted, 0);
        if (pageRequest.getKeyword() != null && !pageRequest.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, pageRequest.getKeyword())
                    .or().like(User::getNickname, pageRequest.getKeyword()));
        }
        wrapper.orderByDesc(User::getCreatedAt);

        var users = userMapper.selectList(wrapper);
        Long total = (long) users.size();

        int offset = pageRequest.getOffset().intValue();
        var pageUsers = users.stream().skip(offset).limit(pageRequest.getPageSize()).toList();

        var dtos = pageUsers.stream().map(u -> {
            UserDTO dto = new UserDTO();
            dto.setId(u.getId().toString());
            dto.setUsername(u.getUsername());
            dto.setNickname(u.getNickname());
            dto.setRole(u.getRole());
            dto.setStatus(u.getStatus());
            dto.setCreatedAt(u.getCreatedAt() != null ? u.getCreatedAt().toString() : null);
            Points latest = pointsMapper.selectOne(new LambdaQueryWrapper<Points>()
                    .eq(Points::getUserId, u.getId())
                    .orderByDesc(Points::getCreatedAt)
                    .last("LIMIT 1"));
            dto.setPointsBalance(latest != null ? latest.getBalance().toString() : "0");
            return dto;
        }).toList();

        return Result.success(new PageResult<>(dtos, total, pageRequest.getPage(), pageRequest.getPageSize()));
    }

    @Transactional
    public Result<?> adjustPoints(Long operatorId, Long userId, Integer amount, String remark) {
        var shopResult = shopService.getMyShop(operatorId);
        if (shopResult.getData() == null || !((Map<?, ?>) shopResult.getData()).containsKey("hasShop")) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }
        Map<?, ?> shopData = (Map<?, ?>) shopResult.getData();
        if (!Boolean.TRUE.equals(shopData.get("hasShop"))) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        User user = userMapper.selectById(userId);
        if (user == null || !operatorId.equals(user.getParentId())) {
            return Result.fail(ResultCode.USER_NOT_FOUND, "用户不存在");
        }

        Points latest = pointsMapper.selectOne(new LambdaQueryWrapper<Points>()
                .eq(Points::getUserId, userId)
                .orderByDesc(Points::getCreatedAt)
                .last("LIMIT 1"));
        int currentBalance = latest != null ? latest.getBalance() : 0;
        int newBalance = currentBalance + amount;
        if (newBalance < 0) {
            return Result.fail(ResultCode.FAIL, "积分不足");
        }

        Points points = new Points();
        points.setId(SnowflakeIdUtil.nextId());
        points.setUserId(userId);
        points.setAmount(amount);
        points.setBalance(newBalance);
        points.setType(amount > 0 ? PointsType.GRANT.getCode() : PointsType.DEDUCT.getCode());
        points.setRemark(remark);
        points.setOperatorId(operatorId);
        pointsMapper.insert(points);

        String title = amount > 0 ? "积分发放" : "积分扣除";
        String content = amount > 0 ? "您收到了" + amount + "积分" : "您被扣除了" + Math.abs(amount) + "积分";
        if (remark != null) content += "，" + remark;

        Message msg = new Message();
        msg.setId(SnowflakeIdUtil.nextId());
        msg.setUserId(userId);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setType(MessageType.POINTS.getCode());
        msg.setRelatedId(points.getId());
        msg.setIsRead(0);
        messageService.insertMessage(msg);

        return Result.success(newBalance);
    }

    public Result<?> getPointsLog(Long operatorId, Long userId, PageRequest pageRequest) {
        var shopResult = shopService.getMyShop(operatorId);
        if (shopResult.getData() == null || !((Map<?, ?>) shopResult.getData()).containsKey("hasShop")) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }
        Map<?, ?> shopData = (Map<?, ?>) shopResult.getData();
        if (!Boolean.TRUE.equals(shopData.get("hasShop"))) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        return getPointsLogInternal(userId, pageRequest);
    }

    // ============ 用户端积分查询 ============
    public Result<?> getPoints(Long userId) {
        Points latest = pointsMapper.selectOne(new LambdaQueryWrapper<Points>()
                .eq(Points::getUserId, userId)
                .orderByDesc(Points::getCreatedAt).last("LIMIT 1"));
        return Result.success(java.util.Collections.singletonMap("points",
                latest != null ? latest.getBalance() : 0));
    }

    public Result<?> getPointsLog(Long userId, PageRequest pageRequest) {
        return getPointsLogInternal(userId, pageRequest);
    }

    private Result<?> getPointsLogInternal(Long userId, PageRequest pageRequest) {
        LambdaQueryWrapper<Points> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Points::getUserId, userId).eq(Points::getDeleted, 0)
               .orderByDesc(Points::getCreatedAt);

        var list = pointsMapper.selectList(wrapper);
        Long total = (long) list.size();

        int offset = pageRequest.getOffset().intValue();
        var pageList = list.stream().skip(offset).limit(pageRequest.getPageSize()).toList();

        var result = pageList.stream().map(p -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", p.getId().toString());
            map.put("amount", p.getAmount());
            map.put("balance", p.getBalance());
            map.put("type", p.getType());
            map.put("typeDesc", getPointsTypeDesc(p.getType()));
            map.put("remark", p.getRemark());
            map.put("createdAt", p.getCreatedAt().toString());
            return map;
        }).toList();

        return Result.success(new PageResult<>(result, total, pageRequest.getPage(), pageRequest.getPageSize()));
    }

    @Transactional
    public Result<?> approveUser(Long operatorId, Long userId) {
        var shopResult = shopService.getMyShop(operatorId);
        if (shopResult.getData() == null || !((Map<?, ?>) shopResult.getData()).containsKey("hasShop")) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }
        Map<?, ?> shopData = (Map<?, ?>) shopResult.getData();
        if (!Boolean.TRUE.equals(shopData.get("hasShop"))) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        User user = userMapper.selectById(userId);
        if (user == null || !operatorId.equals(user.getParentId())) {
            return Result.fail(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
        if (user.getStatus() != UserStatus.PENDING.getCode()) {
            return Result.fail(ResultCode.FAIL, "用户不是待审核状态");
        }

        user.setStatus(UserStatus.NORMAL.getCode());
        userMapper.updateById(user);

        Message msg = new Message();
        msg.setId(SnowflakeIdUtil.nextId());
        msg.setUserId(userId);
        msg.setTitle("账号审核通过");
        msg.setContent("您的账号已审核通过");
        msg.setType(MessageType.ORDER.getCode());
        msg.setIsRead(0);
        messageService.insertMessage(msg);

        return Result.success();
    }

    @Transactional
    public Result<?> rejectUser(Long operatorId, Long userId) {
        var shopResult = shopService.getMyShop(operatorId);
        if (shopResult.getData() == null || !((Map<?, ?>) shopResult.getData()).containsKey("hasShop")) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }
        Map<?, ?> shopData = (Map<?, ?>) shopResult.getData();
        if (!Boolean.TRUE.equals(shopData.get("hasShop"))) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        User user = userMapper.selectById(userId);
        if (user == null || !operatorId.equals(user.getParentId())) {
            return Result.fail(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
        if (user.getStatus() != UserStatus.PENDING.getCode()) {
            return Result.fail(ResultCode.FAIL, "用户不是待审核状态");
        }

        user.setDeleted(1);
        userMapper.updateById(user);

        Message msg = new Message();
        msg.setId(SnowflakeIdUtil.nextId());
        msg.setUserId(userId);
        msg.setTitle("账号审核拒绝");
        msg.setContent("您的账号审核未通过");
        msg.setType(MessageType.ORDER.getCode());
        msg.setIsRead(0);
        messageService.insertMessage(msg);

        return Result.success();
    }

    private String getPointsTypeDesc(Integer type) {
        if (type == null) return "";
        return switch (type) {
            case 1 -> "发放";
            case 2 -> "扣除";
            case 3 -> "兑换";
            case 4 -> "退款";
            default -> "";
        };
    }
}