package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notice_msg")
public class NoticeMsg {
    @TableId(type = IdType.AUTO)
    private Integer msgId;          // 通知ID
    private Integer receiverType;   // 接收者类型：1学生 2老师 3场地管理员
    private Integer receiverId;     // 接收者ID
    private String title;           // 标题
    private String content;         // 内容
    private LocalDateTime sendTime; // 发送时间
    private Integer isRead;         // 是否已读
    private Integer sendBy;         // 发送者（超级管理员ID）
}