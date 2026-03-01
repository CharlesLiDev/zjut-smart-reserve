package com.zjut.backend.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zjut.backend.entity.SysUser;
import com.zjut.backend.service.SysUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DataBootstrapConfig {
    private static final Logger log = LoggerFactory.getLogger(DataBootstrapConfig.class);

    @Bean
    public CommandLineRunner initDefaultUsers(SysUserService sysUserService) {
        return args -> {
            createIfMissing(sysUserService, "student_demo", "123456", "测试学生", "STUDENT", 1, 0, "13000000001");
            createIfMissing(sysUserService, "admin_demo", "123456", "测试场地管理员", "VENUE_ADMIN", 1, 0, "13000000002");
            createIfMissing(sysUserService, "super_demo", "123456", "测试系统管理员", "SYS_ADMIN", 1, 0, "13000000003");
        };
    }

    private void createIfMissing(
            SysUserService sysUserService,
            String username,
            String password,
            String realName,
            String role,
            Integer status,
            Integer isFirstLogin,
            String phoneNumber
    ) {
        try {
            SysUser existing = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUsername, username)
                    .last("limit 1"));
            if (existing != null) {
                return;
            }

            SysUser user = new SysUser();
            user.setUsername(username);
            user.setPassword(password);
            user.setRealName(realName);
            user.setRole(role);
            user.setStatus(status);
            user.setIsFirstLogin(isFirstLogin);
            user.setPhoneNumber(phoneNumber);
            user.setCreateTime(LocalDateTime.now());
            sysUserService.save(user);
        } catch (Exception e) {
            // Seeding users should not block app startup in non-clean environments.
            log.warn("Skip bootstrap user {} due to: {}", username, e.getMessage());
        }
    }
}
