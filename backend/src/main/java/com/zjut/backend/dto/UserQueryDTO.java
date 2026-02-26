package com.zjut.backend.dto;

import lombok.Data;

@Data
public class UserQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String username; // 支持模糊搜索
    private String role;     // 按角色筛选: STUDENT, VENUE_ADMIN
    private Integer status;  // 0-正常, 1-禁用
}