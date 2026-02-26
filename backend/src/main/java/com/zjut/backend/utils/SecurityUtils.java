package com.zjut.backend.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private HttpServletRequest request;

    public Long getCurrentUserId()
    {
        String authHeader = request.getHeader("Authorization");
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            throw new RuntimeException("未发现有效的登录令牌");
        }

        String token=authHeader.substring(7);

        Object userId = jwtUtils.getClaimsByToken(token).get("userId");
        if(userId == null){
            throw new RuntimeException("登录令牌中未发现用户ID");
        }
        return Long.parseLong(userId.toString());
    }

    public String getUserRole()
    {
        // 1. 从请求头获取 Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("用户未登录或令牌无效");
        }
        String token = authHeader.substring(7);

        // 2. 解析 Token 拿到 Claims
        Object role = jwtUtils.getClaimsByToken(token).get("role");

        if (role == null) {
            throw new RuntimeException("令牌中未包含角色信息");
        }

        return role.toString();
    }
}
