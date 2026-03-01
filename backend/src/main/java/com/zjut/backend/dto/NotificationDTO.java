package com.zjut.backend.dto;

import lombok.Data;

@Data
public class NotificationDTO {
    private String title;
    private String content;
    private Integer type;
    private String targetRole;
    private Long targetUserId;
}
