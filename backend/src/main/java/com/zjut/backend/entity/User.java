package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer userId;     // 用户ID
    private String userNo;      // 学号/工号
    private String name;        // 姓名
    private Integer userType;   // 类型：1学生 2老师
    private String phone;       // 电话
    private Integer status;     // 状态：1正常 0禁用
}