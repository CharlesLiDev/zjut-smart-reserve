package com.zjut.backend.dto; // 或者 .vo

import lombok.Data;
import java.io.Serializable;

@Data
public class VenueVO implements Serializable {
    // 1. 直接复用实体类中的基础字段
    private Long id;
    private String name;
    private String type;
    private Integer capacity;
    private String location;
    private String imageUrl;
    private Integer status; // 0-可用, 1-维护中, 2-禁用
    private String description;
    private String equipment;

    // 2. 核心：增加展示用的逻辑字段
    private Long adminId;      // 管理员ID
    private String adminName;  // 管理员真实姓名 (这就是 VO 的意义所在)
}