package com.zjut.backend.dto;

import lombok.Data;

@Data
public class NotificationQueryDTO {
    private Long userId;
    private String role;
    private Integer current = 1;
    private Integer size = 10;
}
