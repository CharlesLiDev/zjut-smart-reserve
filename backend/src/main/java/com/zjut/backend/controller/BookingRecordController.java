package com.zjut.backend.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zjut.backend.common.Result;
import com.zjut.backend.dto.AppointmentDTO;
import com.zjut.backend.dto.AppointmentVO;
import com.zjut.backend.dto.MyAppointmentQueryDTO;
import com.zjut.backend.entity.BookingRecord;
import com.zjut.backend.entity.VenueInfo;
import com.zjut.backend.service.BookingRecordService;
import com.zjut.backend.service.VenueInfoService;
import com.zjut.backend.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appointments")
public class BookingRecordController {

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private BookingRecordService bookingRecordService;

    @Autowired
    private VenueInfoService venueInfoService;

    @PostMapping
    public Result postAppointment(@RequestBody AppointmentDTO dto) {
        Long queryUserId = securityUtils.getCurrentUserId();
        return bookingRecordService.submitAppointment(dto, queryUserId);
    }

    /**
     * 查询用户的预约记录
     * @param queryDTO 查询参数
     * @return 用户的预约记录列表
     */
    @GetMapping
    public Result getMyAppointments(MyAppointmentQueryDTO queryDTO) {

        System.out.println("tab: " + queryDTO.getTab());

        Long userId = securityUtils.getCurrentUserId();

        IPage<AppointmentVO> data = bookingRecordService.getMyAppointments(queryDTO, userId);

        return Result.success(data);
    }

    /**
     * 更新预约记录状态
     * @param id 预约记录ID
     * @param body 包含操作指令的请求体
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public Result updateStatus(@PathVariable Long id, @RequestParam Map<String, String> body) {
        String action = body.get("action");
        Long currentUserId = securityUtils.getCurrentUserId();
        BookingRecord record = bookingRecordService.getById(id);

        if (record == null) {
            return Result.error("预约记录不存在");
        }

        if (!record.getUserId().equals(currentUserId)) {
            return Result.error("你无权操作他人的预约记录");
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

    /**
     * 查询管理员的预约记录
     * @param tab 查询标签（0:待审核, 1:已驳回, 2:已通过, 3:已使用, 4:已取消）
     * @return 管理员的预约记录列表
     */
    @GetMapping("/admin/list")
    public Result getAdminAppointments(@RequestParam Integer tab) {
        Long adminId = securityUtils.getCurrentUserId();
        String role = securityUtils.getUserRole();
        return bookingRecordService.getAdminRecordList(tab, adminId, role);
    }

    /**
     * 管理员审核预约记录
     * @param params 包含 recordId, status, rejectReason 的请求体
     * @return 操作结果
     */
    @PostMapping("/admin/audit")
    public Result audit(@RequestBody Map<String, Object> params) {
        try {
            // 参数解析与类型安全转换
            Long recordId = Long.valueOf(params.get("recordId").toString());
            Integer status = Integer.valueOf(params.get("status").toString()); // 1-驳回, 2-通过
            String rejectReason = (String) params.get("rejectReason");

            Long adminId = securityUtils.getCurrentUserId();

            // 调用你重构后的 Service 方法 (带预留通知逻辑的那个)
            boolean success = bookingRecordService.auditApply(recordId, status, rejectReason, adminId);

            return success ? Result.success("审批成功") : Result.error("审批失败");
        } catch (Exception e) {
            return Result.error("审批异常：" + e.getMessage());
        }
    }

    @GetMapping("/admin/venue-occupancy")
    public Result getVenueOccupancy(@RequestParam Long venueId, @RequestParam String date) {
        Long currentUserId = securityUtils.getCurrentUserId();
        String role = securityUtils.getUserRole();

        // 1. 权限校验：如果是场地管理员，只能查自己管的场馆
        if ("VENUE_ADMIN".equals(role)) {
            VenueInfo venue = venueInfoService.getById(venueId);
            if (venue == null || !currentUserId.equals(venue.getAdminId())) {
                return Result.error("权限不足：您不是该场地的管理员");
            }
        }

        // 2. 查询该场地在那一天的所有“占用中”的记录
        // 占用状态包括：2-已通过（待使用）, 3-已完成（正在使用/已结束）
        List<BookingRecord> occupiedList = bookingRecordService.list(new LambdaQueryWrapper<BookingRecord>()
                .eq(BookingRecord::getVenueId, venueId)
                .eq(BookingRecord::getBookingDate, LocalDate.parse(date))
                .in(BookingRecord::getStatus, 2, 3)
                .orderByAsc(BookingRecord::getTimeSlot)); // 按时间段排序

        return Result.success(occupiedList);
    }

    @GetMapping("/admin/my-venues/today")
    public Result getMyVenuesTodayStatus() {
        Long adminId = securityUtils.getCurrentUserId();

        // 1. 找到我管的所有场地
        List<VenueInfo> myVenues = venueInfoService.list(new LambdaQueryWrapper<VenueInfo>()
                .eq(VenueInfo::getAdminId, adminId));

        if (myVenues.isEmpty()) return Result.success(new ArrayList<>());

        List<Long> venueIds = myVenues.stream().map(VenueInfo::getId).collect(Collectors.toList());

        // 2. 查出这些场地今天的预约情况
        List<BookingRecord> todayRecords = bookingRecordService.list(new LambdaQueryWrapper<BookingRecord>()
                .in(BookingRecord::getVenueId, venueIds)
                .eq(BookingRecord::getBookingDate, LocalDate.now())
                .in(BookingRecord::getStatus, 2, 3));

        return Result.success(todayRecords);
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
