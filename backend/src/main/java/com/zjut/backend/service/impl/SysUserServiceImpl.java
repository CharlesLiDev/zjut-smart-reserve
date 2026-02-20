package com.zjut.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjut.backend.common.Result;
import com.zjut.backend.entity.SysUser;
import com.zjut.backend.service.SysUserService;
import com.zjut.backend.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
* @author 15588
* @description 针对表【sys_user(用户表)】的数据库操作Service实现
* @createDate 2026-02-20 16:39:05
*/
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService{

    @Override
    public Result login(String username, String password) {
        SysUser user = this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if(user==null||!user.getPassword().equals(password)){
            return Result.error("用户名或密码错误");
        }
        if(user.getIsFirstLogin()==1){
            return Result.forcePasswordChange(user);
        }
        return Result.success(user);
    }

    @Override
    public Result register(SysUser user) {
        SysUser existingUser=this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, user.getUsername()));
        if(existingUser!=null){
            return Result.error("该用户已存在");
        }

        user.setRole("STUDENT");
        user.setIsFirstLogin(0);
        user.setCreateTime(LocalDateTime.now());

        if(this.save(user)){
            return Result.success("注册成功");
        }
        return Result.error("注册失败，请联系管理员");
    }
}




