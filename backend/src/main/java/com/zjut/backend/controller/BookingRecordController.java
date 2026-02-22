package com.zjut.backend.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zjut.backend.common.Result;
import com.zjut.backend.dto.AppointmentDTO;
import com.zjut.backend.dto.AppointmentVO;
import com.zjut.backend.dto.MyAppointmentQueryDTO;
import com.zjut.backend.entity.BookingRecord;
import com.zjut.backend.service.BookingRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class BookingRecordController {

    @Autowired
    private BookingRecordService bookingRecordService;

    @PostMapping
    public Result postAppointment(@RequestBody AppointmentDTO dto) {
        Long currentUserId = 1L;//后续从token中获取
        return bookingRecordService.submitAppointment(dto, currentUserId);
    }

    @GetMapping
    public Result getMyAppointments(MyAppointmentQueryDTO queryDTO) {

        System.out.println("tab: " + queryDTO.getTab());

        Long userId = 1L;//后续从token中获取

        IPage<AppointmentVO> data = bookingRecordService.getMyAppointments(queryDTO, userId);

        return Result.success(data);
    }

    @PutMapping("/{id}/status")
    public Result updateStatus(@PathVariable Long id, @RequestParam Map<String, String> body) {
        String action = body.get("action");
        BookingRecord record = bookingRecordService.getById(id);

        if (record == null) {
            return Result.error("预约记录不存在");
        }

        if ("withdraw".equals(action)) {
            if (record.getStatus() != 0) {
                return Result.error("非待审核状态，无法撤回");
            }
            record.setStatus(4);//已取消
        } else if ("cancel".equals(action)) {
            if (record.getStatus() != 2) return Result.error("当前状态不支持取消操作");
            if (!checkCanCancel(record.getBookingDate(), record.getTimeSlot())) {
                return Result.error("距离预约开始不足2小时，请联系管理员处理");
            }
            record.setStatus(4);
        } else {
            return Result.error("无效的操作指令");
        }

        return bookingRecordService.updateById(record) ? Result.success("操作成功") : Result.error("操作失败");
    }

    private boolean checkCanCancel(java.time.LocalDate date, String timeSlot) {
        try {

            String startTimeStr = timeSlot.split("-")[0].trim();
            LocalDateTime startDateTime = LocalDateTime.of(date, LocalTime.parse(startTimeStr));
            LocalDateTime deadLine = LocalDateTime.now().plusHours(2);

            boolean allowed = deadLine.isBefore(startDateTime);

            System.out.println("预约开始时间: " + startDateTime);
            System.out.println("当前死线时间: " + deadLine);
            System.out.println("是否允许取消: " + allowed);

            return allowed;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
