package com.zjut.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjut.backend.entity.SysUser;
import com.zjut.backend.service.SysUserService;
import com.zjut.backend.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

/**
* @author 15588
* @description 针对表【sys_user(用户表)】的数据库操作Service实现
* @createDate 2026-02-20 16:39:05
*/
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
    implements SysUserService{

}




