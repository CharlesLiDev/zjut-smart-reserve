package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.SysAdmin;

public interface SysAdminService extends IService<SysAdmin> {
    // 超级管理员登录
    String login(String username, String password);
    // 发布全局公告（简化版，实际需推送给所有用户）
    boolean publishNotice(String title, String content, Integer sysAdminId);
    // 发送单独通知
    boolean sendMsg(Integer receiverType, Integer receiverId, String title, String content, Integer sendBy);
}