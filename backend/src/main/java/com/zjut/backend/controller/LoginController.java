package com.zjut.backend.controller;

import com.zjut.backend.common.Result;
import com.zjut.backend.entity.SysUser;
import com.zjut.backend.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private SysUserService userService;
    @Autowired
    private SysUserService sysUserService;

    @PostMapping("/login")
    public Result login(@RequestBody SysUser loginUser){
        String username=loginUser.getUsername();
        String password=loginUser.getPassword();

        if(username==null||password==null){
            return Result.error("用户名或密码不能为空");
        }

        return userService.login(username,password);
    }

    @PostMapping("/updatePassword")
    public Result updatePassword(@RequestBody SysUser user){
        if(user.getId()==null||user.getPassword()==null){
            return Result.error("参数不完整");
        }

        user.setIsFirstLogin(0);

        if(userService.updateById(user)){
            return Result.success("密码修改成功，请重新登录");
        }
        return Result.error("密码修改失败");
    }

    @PostMapping("/register")
    public Result register(@RequestBody SysUser user){
        if(user.getUsername()==null||user.getPassword()==null){
            return Result.error("请填写完整的注册信息");
        }
        return sysUserService.register(user);
    }

    @PostMapping("/logout")
    public Result logout(){
        return Result.success("退出成功");
    }
}
