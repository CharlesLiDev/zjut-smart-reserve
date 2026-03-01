package com.zjut.backend.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjut.backend.common.Result;
import com.zjut.backend.dto.NotificationDTO;
import com.zjut.backend.dto.NotificationQueryDTO;
import com.zjut.backend.dto.ReadNotificationDTO;
import com.zjut.backend.entity.Notification;
import com.zjut.backend.entity.SysUser;
import com.zjut.backend.service.NotificationService;
import com.zjut.backend.service.SysUserService;
import com.zjut.backend.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Autowired
    private SecurityUtils securityUtils;
    @Autowired
    private SysUserService sysUserService;

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

        String role = normalizeRole(dto.getTargetRole());
        Integer type = dto.getType() == null ? 0 : dto.getType();

        if (role == null) {
            Notification notice = buildNotice(dto.getTitle(), dto.getContent(), type, null);
            notificationService.save(notice);
            return Result.success("公告已全校发布");
        }

        List<SysUser> users = sysUserService.lambdaQuery()
                .eq(SysUser::getRole, role)
                .select(SysUser::getId)
                .list();
        for (SysUser user : users) {
            Notification notice = buildNotice(dto.getTitle(), dto.getContent(), type, user.getId());
            notificationService.save(notice);
        }
        return Result.success("公告已定向发布");
    }

    @PostMapping("/system/direct")
    public Result sendDirectNotice(@RequestBody NotificationDTO dto) {
        if(!"SYS_ADMIN".equals(securityUtils.getUserRole())) {
            return Result.error("只有系统管理员才能发送通知");
        }
        if (dto.getTargetUserId() == null) {
            return Result.error("请选择接收用户");
        }
        Integer type = dto.getType() == null ? 2 : dto.getType();
        Notification notice = buildNotice(dto.getTitle(), dto.getContent(), type, dto.getTargetUserId());
        notificationService.save(notice);
        return Result.success("通知已发送");
    }

    private Notification buildNotice(String title, String content, Integer type, Long targetUserId) {
        Notification notice = new Notification();
        String safeTitle = title == null ? "" : title.trim();
        if (type != null && type == 0) {
            notice.setTitle("【系统公告】" + safeTitle);
        } else {
            notice.setTitle(safeTitle);
        }
        notice.setContent(content);
        notice.setType(type == null ? 0 : type);
        notice.setTargetUserId(targetUserId);
        notice.setCreateTime(LocalDateTime.now());
        return notice;
    }

    private String normalizeRole(String rawRole) {
        if (rawRole == null || rawRole.trim().isEmpty()) return null;
        String role = rawRole.trim().toUpperCase();
        if ("USER".equals(role) || "STUDENT".equals(role) || "TEACHER".equals(role)) return "STUDENT";
        if ("ADMIN".equals(role) || "VENUE_ADMIN".equals(role)) return "VENUE_ADMIN";
        if ("SUPER_ADMIN".equals(role) || "SYS_ADMIN".equals(role)) return "SYS_ADMIN";
        return null;
    }
}
