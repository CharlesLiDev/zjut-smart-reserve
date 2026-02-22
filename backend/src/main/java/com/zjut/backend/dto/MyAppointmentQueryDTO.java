package com.zjut.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyAppointmentQueryDTO {
    //当前页码，默认第一页
    private Integer current = 1;
    //每页数量，默认10条
    private Integer size = 10;
    //ongoing(进行中), ended(已结束), rejected(已驳回)
    private String tab;
}
