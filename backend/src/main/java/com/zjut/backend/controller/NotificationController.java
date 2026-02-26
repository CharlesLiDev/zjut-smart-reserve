package com.zjut.backend.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjut.backend.common.Result;
import com.zjut.backend.dto.NotificationDTO;
import com.zjut.backend.dto.NotificationQueryDTO;
import com.zjut.backend.dto.ReadNotificationDTO;
import com.zjut.backend.entity.Notification;
import com.zjut.backend.service.NotificationService;
import com.zjut.backend.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Autowired
    private SecurityUtils securityUtils;

    // 获取用户通知列表(包括广播信息）
    @GetMapping
    public Result getUserNotifications(NotificationQueryDTO queryDTO) {
        Long currentUserId = securityUtils.getCurrentUserId();
        queryDTO.setUserId(currentUserId);

        IPage<Notification> data = notificationService.getUserNotifications(queryDTO);
        return Result.success(data);
    }

    @PostMapping("/read")
    public Result markAsRead(@RequestBody ReadNotificationDTO readDto) {
        Long currentUserId = securityUtils.getCurrentUserId();

        if (readDto.getIds() == null || readDto.getIds().isEmpty()) {
            return Result.error("未选择任何通知");
        }

        notificationService.markAsRead(readDto.getIds(), currentUserId);
        return Result.success("标记成功");
    }

    // 获取用户个人通知列表（不包括广播信息）
    @GetMapping("/my")
    public Result getMyNotifications() {
        Long currentUserId = securityUtils.getCurrentUserId();
        return Result.success(notificationService.list(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getTargetUserId, currentUserId)
                        .orderByDesc(Notification::getCreateTime)
        ));
    }

    @PostMapping("/system/broadcast")
    public Result broadcastNotice(@RequestBody NotificationDTO dto) {
        if(!"SYS_ADMIN".equals(securityUtils.getUserRole())) {
            return Result.error("只有系统管理员才能广播通知");
        }

        Notification notice = new Notification();
        notice.setTitle("【系统公告】" + dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setType(0); // 0 代表系统公告
        notice.setTargetUserId(null); // targetUserId 为空表示所有人可见
        notice.setCreateTime(LocalDateTime.now());

        notificationService.save(notice);
        return Result.success("公告已全校发布");
    }
}
