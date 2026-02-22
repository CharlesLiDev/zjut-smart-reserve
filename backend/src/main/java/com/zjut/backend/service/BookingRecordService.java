package com.zjut.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zjut.backend.common.Result;
import com.zjut.backend.dto.AppointmentDTO;
import com.zjut.backend.dto.AppointmentVO;
import com.zjut.backend.dto.MyAppointmentQueryDTO;
import com.zjut.backend.entity.BookingRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 15588
* @description 针对表【booking_record(预约申请表)】的数据库操作Service
* @createDate 2026-02-20 16:39:05
*/
public interface BookingRecordService extends IService<BookingRecord> {
    Result submitAppointment(AppointmentDTO dto, Long userId);
    IPage<AppointmentVO> getMyAppointments(MyAppointmentQueryDTO queryDTO, Long userId);
}
