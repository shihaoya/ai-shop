package com.sh.aishop.message.controller;

import com.sh.aishop.common.Result;
import com.sh.aishop.dto.PageRequest;
import com.sh.aishop.message.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "消息管理", description = "用户/运营人员消息操作")
@RestController
@RequestMapping("/api/operator")
public class MessageController {
    @Autowired
    private MessageService messageService;

    @Operation(summary = "消息列表", description = "获取系统消息列表")
    @GetMapping("/messages")
    public Result<?> getMessages(HttpServletRequest request, PageRequest pageRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return messageService.getMessages(userId, pageRequest);
    }

    @Operation(summary = "标记已读", description = "将消息标记为已读")
    @PutMapping("/messages/{id}/read")
    public Result<?> markMessageRead(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return messageService.markMessageRead(userId, id);
    }
}