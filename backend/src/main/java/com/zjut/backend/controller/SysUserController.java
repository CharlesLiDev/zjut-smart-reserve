package com.zjut.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjut.backend.common.Result;
import com.zjut.backend.dto.UserQueryDTO;
import com.zjut.backend.entity.SysUser;
import com.zjut.backend.service.SysUserService;
import com.zjut.backend.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class SysUserController {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SecurityUtils securityUtils;

    @GetMapping("/venue-admins")
    public Result getAllVenueAdmins() {
        if(!"SYS_ADMIN".equals(securityUtils.getUserRole())) {
            return Result.error("权限不足");
        }

        List<SysUser> admins=sysUserService.list(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRole, "VENUE_ADMIN")
                .select(SysUser::getId, SysUser::getUsername, SysUser::getRealName)); // 只查必要的字段

        return Result.success(admins);
    }

    // 获取用户列表 (分页、模糊搜索、按角色、按状态)
    @GetMapping("/list")
    public Result getUserList(UserQueryDTO queryDTO) {
        if (!"SYS_ADMIN".equals(securityUtils.getUserRole())) {
            return Result.error("权限不足");
        }

        Page<SysUser> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        // 按用户名模糊搜索
        if (StringUtils.hasText(queryDTO.getUsername())) {
            wrapper.like(SysUser::getUsername, queryDTO.getUsername());
        }
        // 按角色筛选
        if (StringUtils.hasText(queryDTO.getRole())) {
            wrapper.eq(SysUser::getRole, queryDTO.getRole());
        }
        // 按状态筛选 (0-正常, 1-禁用)
        if (queryDTO.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, queryDTO.getStatus());
        }

        wrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> result = sysUserService.page(page, wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(result);
    }


     //修改用户角色 (例如：任命学生为场地管理员)

    @PutMapping("/{id}/role")
    public Result updateUserRole(@PathVariable Long id, @RequestParam String newRole) {
        if (!"SYS_ADMIN".equals(securityUtils.getUserRole())) {
            return Result.error("权限不足");
        }

        SysUser user = sysUserService.getById(id);
        if (user == null) return Result.error("用户不存在");

        user.setRole(newRole);
        sysUserService.updateById(user);
        return Result.success("角色升级成功");
    }


     //修改账号状态 (禁用/启用)

    @PutMapping("/{id}/status")
    public Result updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (!"SYS_ADMIN".equals(securityUtils.getUserRole())) {
            return Result.error("权限不足");
        }

        SysUser user = sysUserService.getById(id);
        if (user == null) return Result.error("用户不存在");

        user.setStatus(status);
        sysUserService.updateById(user);

        String action = (status == 1) ? "启用" : "禁用";
        return Result.success("账号已" + action);
    }

    @PutMapping("/{id}/reset-password")
    public Result resetPassword(@PathVariable Long id) {
        if (!"SYS_ADMIN".equals(securityUtils.getUserRole())) {
            return Result.error("权限不足");
        }

        SysUser user = sysUserService.getById(id);
        if (user == null) return Result.error("用户不存在");

        if ("SYS_ADMIN".equals(String.valueOf(user.getRole()))) {
            return Result.error("不允许重置系统管理员密码");
        }

        user.setPassword("123456");
        user.setIsFirstLogin(1);
        sysUserService.updateById(user);

        return Result.success("密码已重置为 123456");
    }
}
