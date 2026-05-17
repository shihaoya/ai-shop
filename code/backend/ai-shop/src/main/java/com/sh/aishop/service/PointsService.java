package com.sh.aishop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sh.aishop.common.Result;
import com.sh.aishop.dto.PageRequest;
import com.sh.aishop.dto.PageResult;
import com.sh.aishop.entity.Points;
import com.sh.aishop.mapper.PointsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PointsService {
    @Autowired
    private PointsMapper pointsMapper;

    /**
     * 获取用户积分余额
     */
    public Result<?> getPointsBalance(Long userId) {
        Points latest = pointsMapper.selectOne(new LambdaQueryWrapper<Points>()
                .eq(Points::getUserId, userId)
                .orderByDesc(Points::getCreatedAt).last("LIMIT 1"));
        return Result.success(Map.of("points", latest != null ? latest.getBalance() : 0));
    }

    /**
     * 获取积分变动记录
     */
    public Result<?> getPointsLog(Long userId, PageRequest pageRequest) {
        LambdaQueryWrapper<Points> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Points::getUserId, userId).eq(Points::getDeleted, 0)
               .orderByDesc(Points::getCreatedAt);

        List<Points> list = pointsMapper.selectList(wrapper);
        Long total = (long) list.size();

        int offset = pageRequest.getOffset().intValue();
        list = list.stream().skip(offset).limit(pageRequest.getPageSize()).collect(Collectors.toList());

        List<Map<String, Object>> result = list.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId().toString());
            map.put("amount", p.getAmount());
            map.put("balance", p.getBalance());
            map.put("type", p.getType());
            map.put("typeDesc", getPointsTypeDesc(p.getType()));
            map.put("remark", p.getRemark());
            map.put("createdAt", p.getCreatedAt().toString());
            return map;
        }).collect(Collectors.toList());

        return Result.success(new PageResult<>(result, total, pageRequest.getPage(), pageRequest.getPageSize()));
    }

    private String getPointsTypeDesc(Integer type) {
        if (type == null) return "未知";
        return switch (type) {
            case 1 -> "积分发放";
            case 2 -> "积分扣除";
            case 3 -> "积分兑换";
            case 4 -> "退款返还";
            default -> "其他";
        };
    }
}