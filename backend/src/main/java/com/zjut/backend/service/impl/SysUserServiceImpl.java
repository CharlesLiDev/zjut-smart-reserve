package com.zjut.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjut.backend.common.Result;
import com.zjut.backend.entity.SysUser;
import com.zjut.backend.service.SysUserService;
import com.zjut.backend.mapper.SysUserMapper;
import com.zjut.backend.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
* @author 15588
* @description 针对表【sys_user(用户表)】的数据库操作Service实现
* @createDate 2026-02-20 16:39:05
*/
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService{

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public Result login(String username, String password) {
        SysUser user = this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));

        if (user == null) {
            return Result.error("用户不存在");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error("该账号已被禁用，请联系管理员");
        }

        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        boolean match = password.equals(user.getPassword()) || md5Password.equalsIgnoreCase(user.getPassword());
        if (!match) {
            return Result.error("用户名或密码错误");
        }
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());

        String token = jwtUtils.generateToken(claims);

        if(user.getIsFirstLogin() != null && user.getIsFirstLogin() == 1){
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            user.setPassword(null);
            data.put("token", token);
            data.put("user", user);
            return Result.forcePasswordChange(data);
        }

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("token", token);
        user.setPassword(null);
        data.put("user", user);

        return Result.success(data);
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

