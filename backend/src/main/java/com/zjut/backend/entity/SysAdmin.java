package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_admin")
public class SysAdmin {
    @TableId(type = IdType.AUTO)
    private Integer sysAdminId; // 超级管理员ID
    private String username;    // 登录账号
    private String password;    // 密码（MD5）
    private String name;        // 姓名
    private LocalDateTime createTime; // 创建时间
    private Integer status;     // 状态：1正常 0禁用
}