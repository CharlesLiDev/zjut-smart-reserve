package com.example.controller;

import com.example.entity.User;
import com.example.service.SysAdminService;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sys-admin")
@RequiredArgsConstructor
@CrossOrigin
public class SysAdminController {

    private final SysAdminService sysAdminService;
    private final UserService userService;

    // 超级管理员登录
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            String token = sysAdminService.login(params.get("username"), params.get("password"));
            result.put("code", 200);
            result.put("msg", "登录成功");
            result.put("token", token);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    // 发布全局公告
    @PostMapping("/publish-notice")
    public Map<String, Object> publishNotice(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = sysAdminService.publishNotice(
                    (String) params.get("title"),
                    (String) params.get("content"),
                    (Integer) params.get("sysAdminId")
            );
            result.put("code", success ? 200 : 500);
            result.put("msg", success ? "发布成功" : "发布失败");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    // 发送单独通知
    @PostMapping("/send-msg")
    public Map<String, Object> sendMsg(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = sysAdminService.sendMsg(
                    (Integer) params.get("receiverType"),
                    (Integer) params.get("receiverId"),
                    (String) params.get("title"),
                    (String) params.get("content"),
                    (Integer) params.get("sendBy")
            );
            result.put("code", success ? 200 : 500);
            result.put("msg", success ? "发送成功" : "发送失败");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    // 查询学生/老师列表
    @GetMapping("/user/list")
    public Map<String, Object> getUserList(@RequestParam Integer userType) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<User> list = userService.selectByType(userType);
            result.put("code", 200);
            result.put("data", list);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    // 退出登录
    @PostMapping("/logout")
    public Map<String, Object> logout() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "退出成功");
        return result;
    }
}