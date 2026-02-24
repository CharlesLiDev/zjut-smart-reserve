package com.zjut.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReadNotificationDTO {
    private List<Long> ids;
}
