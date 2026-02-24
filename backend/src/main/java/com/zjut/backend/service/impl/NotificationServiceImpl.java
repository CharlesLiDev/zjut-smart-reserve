package com.zjut.backend.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjut.backend.dto.NotificationQueryDTO;
import com.zjut.backend.entity.Notification;
import com.zjut.backend.entity.NotificationRead;
import com.zjut.backend.service.NotificationReadService;
import com.zjut.backend.service.NotificationService;
import com.zjut.backend.mapper.NotificationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author 15588
* @description 针对表【notification】的数据库操作Service实现
* @createDate 2026-02-24 10:39:57
*/
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
    implements NotificationService{

    private final NotificationReadService notificationReadService;

    public NotificationServiceImpl(NotificationReadService notificationReadService) {
        this.notificationReadService = notificationReadService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsRead(List<Long> ids, Long userId) {

        if (ids == null || ids.isEmpty()) {
            return true;
        }

        List<Long> alreadyReadIds=notificationReadService.lambdaQuery()
                .eq(NotificationRead::getUserId,userId)
                .in(NotificationRead::getNotificationId,ids)
                .list()
                .stream()
                .map(NotificationRead::getNotificationId)
                .collect(Collectors.toList());

        List<NotificationRead> readList=ids.stream()
                .filter(id->!alreadyReadIds.contains(id))// 只保留没读过的
                .map(notifyId->{
            NotificationRead record=new NotificationRead();
            record.setNotificationId(notifyId);
            record.setUserId(userId);
            record.setReadTime(LocalDateTime.now());
            return record;
        }).collect(Collectors.toList());

        if (!readList.isEmpty()) {
            notificationReadService.saveBatch(readList);
        }
        return true;
    }

    @Override
    public IPage<Notification> getUserNotifications(NotificationQueryDTO queryDTO) {
        // 这里需要调用 Mapper 里的自定义 SQL 或使用 MP 的复杂查询
        // 建议直接调用我们接下来要写的 Mapper 方法
        Page<Notification> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        return baseMapper.selectUserNotifications(page, queryDTO.getUserId());
    }

}




