package com.sh.aishop.message.service;

import com.sh.aishop.common.Result;
import com.sh.aishop.common.entity.Message;
import com.sh.aishop.common.dto.PageRequest;
import com.sh.aishop.common.dto.PageResult;
import com.sh.aishop.message.mapper.MessageMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService implements IMessageService {
    @Autowired
    private MessageMapper messageMapper;

    public Result<?> getMessages(Long userId, PageRequest pageRequest) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId).eq(Message::getDeleted, 0)
               .orderByDesc(Message::getCreatedAt);

        List<Message> messages = messageMapper.selectList(wrapper);
        Long total = (long) messages.size();

        int offset = pageRequest.getOffset().intValue();
        messages = messages.stream().skip(offset).limit(pageRequest.getPageSize()).collect(Collectors.toList());

        List<java.util.Map<String, Object>> result = messages.stream().map(m -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", m.getId().toString());
            map.put("title", m.getTitle());
            map.put("content", m.getContent());
            map.put("type", m.getType());
            map.put("isRead", m.getIsRead());
            map.put("createdAt", m.getCreatedAt().toString());
            return map;
        }).collect(Collectors.toList());

        return Result.success(new PageResult<>(result, total, pageRequest.getPage(), pageRequest.getPageSize()));
    }

    @Transactional
    public Result<?> markMessageRead(Long userId, Long messageId) {
        Message message = messageMapper.selectById(messageId);
        if (message != null && userId.equals(message.getUserId())) {
            message.setIsRead(1);
            messageMapper.updateById(message);
        }
        return Result.success();
    }

    @Transactional
    public void insertMessage(Message message) {
        messageMapper.insert(message);
    }

    public Result<?> getUnreadCount(Long userId) {
        Long count = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0)
                .eq(Message::getDeleted, 0));
        return Result.success(java.util.Collections.singletonMap("count", count));
    }
}