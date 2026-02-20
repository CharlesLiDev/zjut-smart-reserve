package com.zjut.backend.service;

import com.zjut.backend.common.Result;
import com.zjut.backend.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 15588
* @description 针对表【sys_user(用户表)】的数据库操作Service
* @createDate 2026-02-20 16:39:05
*/
public interface SysUserService extends IService<SysUser> {
    Result login(String username, String password);
    Result register(SysUser user);
}
