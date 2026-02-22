package com.zjut.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AppointmentVO {
    private Long id;
    private String eventName;
    private LocalDate bookingDate;
    private String timeSlot;
    private Integer status; // 0-待审核, 1-已驳回, 2-已通过, 4-已取消, 5-已结束

    private Boolean canWithdraw; // 是否可撤回
    private Boolean canCancel;   // 是否可取消
    private String statusDesc;   // 状态文字描述（如：待审核、进行中等）
    // 签到码等 P2 功能预留字段
    private String qrCode;
}
