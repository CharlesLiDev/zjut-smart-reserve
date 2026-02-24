package com.zjut.backend.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjut.backend.common.Result;
import com.zjut.backend.dto.NotificationQueryDTO;
import com.zjut.backend.dto.ReadNotificationDTO;
import com.zjut.backend.entity.Notification;
import com.zjut.backend.service.NotificationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public Result getUserNotifications(NotificationQueryDTO queryDTO) {
        Long currentUserId = 1L;
        queryDTO.setUserId(currentUserId);

        IPage<Notification> data = notificationService.getUserNotifications(queryDTO);
        return Result.success(data);
    }

    @PostMapping("/read")
    public Result markAsRead(@RequestBody ReadNotificationDTO readDto) {
        Long currentUserId = 1L;

        if (readDto.getIds() == null || readDto.getIds().isEmpty()) {
            return Result.error("未选择任何通知");
        }

        notificationService.markAsRead(readDto.getIds(), currentUserId);
        return Result.success("标记成功");
    }
}
