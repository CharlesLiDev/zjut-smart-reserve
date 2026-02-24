package com.zjut.backend.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjut.backend.entity.Notification;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
* @author 15588
* @description 针对表【notification】的数据库操作Mapper
* @createDate 2026-02-24 10:39:57
* @Entity com.zjut.backend.entity.Notification
*/
public interface NotificationMapper extends BaseMapper<Notification> {
    IPage<Notification> selectUserNotifications(IPage<Notification> page, @Param("userId") Long userId);
}




