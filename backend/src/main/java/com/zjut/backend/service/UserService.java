package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.User;

import java.util.List;

public interface UserService extends IService<User> {
    // 根据类型查询用户（1学生 2老师）
    List<User> selectByType(Integer userType);
}