package com.zjut.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zjut.backend.dto.NotificationQueryDTO;
import com.zjut.backend.entity.Notification;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 15588
* @description 针对表【notification】的数据库操作Service
* @createDate 2026-02-24 10:39:57
*/
public interface NotificationService extends IService<Notification> {
    IPage<Notification> getUserNotifications(NotificationQueryDTO queryDTO);
    boolean markAsRead(List<Long> ids, Long userId);
}
