package com.sh.aishop.message.service;

import com.sh.aishop.common.Result;
import com.sh.aishop.common.entity.Message;
import com.sh.aishop.common.dto.PageRequest;

public interface IMessageService {
    Result<?> getMessages(Long userId, PageRequest pageRequest);
    Result<?> markMessageRead(Long userId, Long messageId);
    void insertMessage(Message message);
    Result<?> getUnreadCount(Long userId);
}