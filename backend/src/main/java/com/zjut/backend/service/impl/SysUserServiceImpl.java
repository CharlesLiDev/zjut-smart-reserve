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

    private final String JWT_SECRET = "campusVenueAdmin2026_SecureKey_LongerThan32Chars";
    private final long JWT_EXPIRE = 3600000L; // 1小时有效

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

        String encryptPwd;
        if ("SYS_ADMIN".equals(user.getRole())) {
            // 管理员：强制使用 MD5 校验
            encryptPwd = org.springframework.util.DigestUtils.md5DigestAsHex(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } else {
            // 学生：如果你之前的学生密码是明文，这里直接用 password；如果也是 MD5，就统一加密
            encryptPwd = password;
        }

        if (!user.getPassword().equals(encryptPwd)) {
            return Result.error("用户名或密码错误");
        }
        if(user.getIsFirstLogin() != null && user.getIsFirstLogin() == 1){
            return Result.forcePasswordChange(user);
        }

        javax.crypto.SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(JWT_SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String token = io.jsonwebtoken.Jwts.builder()
                .setSubject(user.getId().toString()) // 以用户ID作为主体
                .claim("role", user.getRole())      // 把角色存入 token，方便前端识别
                .setExpiration(new java.util.Date(System.currentTimeMillis() + JWT_EXPIRE))
                .signWith(key)
                .compact();

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("token", token);
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




