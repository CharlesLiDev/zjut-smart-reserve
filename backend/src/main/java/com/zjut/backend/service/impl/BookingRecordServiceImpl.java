package com.zjut.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjut.backend.common.Result;
import com.zjut.backend.dto.BlockTimeDTO;
import com.zjut.backend.dto.AppointmentDTO;
import com.zjut.backend.dto.AppointmentVO;
import com.zjut.backend.dto.MyAppointmentQueryDTO;
import com.zjut.backend.entity.BookingRecord;
import com.zjut.backend.entity.Notification;
import com.zjut.backend.entity.SysUser;
import com.zjut.backend.entity.VenueInfo;
import com.zjut.backend.service.BookingRecordService;
import com.zjut.backend.mapper.BookingRecordMapper;
import com.zjut.backend.service.NotificationService;
import com.zjut.backend.service.SysUserService;
import com.zjut.backend.service.VenueInfoService;
import com.zjut.backend.utils.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 15588
 * @description 针对表【booking_record(预约申请表)】的数据库操作Service实现
 * @createDate 2026-02-20 16:39:05
 */
@Service
public class BookingRecordServiceImpl extends ServiceImpl<BookingRecordMapper, BookingRecord>
        implements BookingRecordService {
    private final BookingRecordMapper bookingRecordMapper;
    private final VenueInfoService venueInfoService;
    private final NotificationService notificationService;
    private final SysUserService sysUserService;

    @Autowired
    private SecurityUtils securityUtils;

    public BookingRecordServiceImpl(BookingRecordMapper bookingRecordMapper, VenueInfoService venueInfoService, NotificationService notificationService, SysUserService sysUserService) {
        this.bookingRecordMapper = bookingRecordMapper;
        this.venueInfoService = venueInfoService;
        this.notificationService = notificationService;
        this.sysUserService = sysUserService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result submitAppointment(AppointmentDTO dto, Long userId) {

        if (dto == null || dto.getVenueId() == null || dto.getTimeSlot() == null) {
            return Result.error("提交参数不完整");
        }

        //解析时间段字符串
        String[] times = dto.getTimeSlot().split("-");
        if (times.length != 2) {
            return Result.error("时间段格式错误，应为 HH:mm-HH:mm");
        }
        String newStart = times[0].trim();
        String newEnd = times[1].trim();

        //并发冲突检查
        LambdaQueryWrapper<BookingRecord> wrapper = new LambdaQueryWrapper<BookingRecord>();
        wrapper.eq(BookingRecord::getVenueId, dto.getVenueId())
                .eq(BookingRecord::getBookingDate, dto.getBookingDate())
                .in(BookingRecord::getStatus, 0, 2, 3)
                .and(w -> w.apply("SUBSTRING_INDEX(time_slot, '-', 1) < {0}", newEnd)
                        .apply("SUBSTRING_INDEX(time_slot, '-', -1) > {0}", newStart));

        Long count = bookingRecordMapper.selectCount(wrapper);
        if (count > 0) {
            return Result.error("该时段已被占用或正在审核中，请刷新排期表重选");
        }

        BookingRecord record = new BookingRecord();
        BeanUtils.copyProperties(dto, record);
        record.setUserId(userId);
        record.setStatus(0);
        record.setAuditadminid(0L); // Pending approval; DB column is NOT NULL
        record.setCreateTime(LocalDateTime.now());

        if (!StringUtils.hasText(record.getContactPerson()) || !StringUtils.hasText(record.getContactPhone())) {
            SysUser user = sysUserService.getById(userId);
            if (user != null) {
                if (!StringUtils.hasText(record.getContactPerson())) {
                    record.setContactPerson(user.getRealName());
                }
                if (!StringUtils.hasText(record.getContactPhone())) {
                    record.setContactPhone(user.getPhoneNumber());
                }
            }
        }

        bookingRecordMapper.insert(record);
        return Result.success("预约申请已提交，请等待管理员审核");
    }

    @Override
    public IPage<AppointmentVO> getMyAppointments(MyAppointmentQueryDTO queryDTO, Long userId) {
        //初始化分页参数
        Page<BookingRecord> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        //构建查询条件
        LambdaQueryWrapper<BookingRecord> wrapper = new LambdaQueryWrapper<BookingRecord>();
        wrapper.eq(BookingRecord::getUserId, userId);

        // 1. 根据 Tab 过滤状态
        switch (queryDTO.getTab()) {
            case "ongoing": // 进行中：待审核(0) 和 已通过(2)
                wrapper.in(BookingRecord::getStatus, 0, 2);
                break;
            case "ended":   // 已结束：已使用/已结束(3/5)
                wrapper.in(BookingRecord::getStatus, 3, 5);
                break;
            case "rejected": // 已驳回：被驳回(1)
                wrapper.eq(BookingRecord::getStatus, 1);
                break;
        }
        wrapper.orderByDesc(BookingRecord::getCreateTime);

        IPage<BookingRecord> recordPage = this.page(page, wrapper);

        //将查询结果转换为VO
        IPage<AppointmentVO> voPage = recordPage.convert(record -> {
            AppointmentVO vo = new AppointmentVO();
            BeanUtils.copyProperties(record, vo);

            // 计算按钮显示逻辑
            // 撤回：仅在待审核(0)状态
            vo.setCanWithdraw(record.getStatus() == 0);

            // 取消规则：状态为已通过(2) 且 距离开始时间 > 2小时
            if (record.getStatus() == 2) {
                vo.setCanCancel(checkCanCancel(record.getBookingDate(), record.getTimeSlot()));
            } else {
                vo.setCanCancel(false);
            }

            return vo;
        });
        return voPage;
    }

    @Override
    public Integer calculateAvailabilityStatus(Long venueId) {

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        String currentTimeStr = now.toString().substring(0, 5); // 获取 "HH:mm" 格式

        // 在方法开始处
        System.out.println("--- 开始判定场地 ID: " + venueId + " ---");
        System.out.println("当前系统时间字符串: [" + currentTimeStr + "]");

        // 1. 获取该场地今日所有生效预约 (参考你代码中的状态 0, 2, 3)
        LambdaQueryWrapper<BookingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookingRecord::getVenueId, venueId)
                .eq(BookingRecord::getBookingDate, today)
                .in(BookingRecord::getStatus, 0, 2, 3);

        java.util.List<BookingRecord> todayBookings = this.list(wrapper);

        // 2. 定义系统支持的所有时段 (请根据你的实际排期表修改)
        java.util.List<String> allSystemSlots = java.util.Arrays.asList(
                "08:00-10:00", "10:00-12:00", "13:00-15:00", "15:00-17:00", "18:00-20:00"
        );

        boolean isNowOccupied = false;
        boolean hasFutureFree = false;

        // 3. 判定当前是否被占用
        for (BookingRecord br : todayBookings) {
            String[] parts = br.getTimeSlot().split("-");
            if (currentTimeStr.compareTo(parts[0].trim()) >= 0 && currentTimeStr.compareTo(parts[1].trim()) < 0) {
                isNowOccupied = true;
                break;
            }

            System.out.println("检查预约记录时段: [" + br.getTimeSlot() + "]");
        }

        // 4. 判定今天接下来是否有空闲时段
        for (String slot : allSystemSlots) {
            String slotStart = slot.split("-")[0];
            // 只看现在的时点之后的段
            if (slotStart.compareTo(currentTimeStr) > 0) {
                // 检查这个段是否在已预约列表里
                boolean booked = todayBookings.stream()
                        .anyMatch(b -> b.getTimeSlot().equals(slot));
                if (!booked) {
                    hasFutureFree = true;
                    break;
                }
            }
        }

        // 5. 返回状态码
        if (!isNowOccupied && hasFutureFree) return 0; // 可预约 (蓝色)
        if (isNowOccupied && hasFutureFree) return 1;  // 稍后有空 (橙色)
        return 2; // 今日满约 (灰色)
    }

    @Override
    @Transactional
    public boolean auditApply(Long recordId, Integer status, String rejectReason, Long adminId) {
        BookingRecord record = getById(recordId);
        if (record == null) throw new RuntimeException("记录不存在");

        String role = securityUtils.getUserRole();

        // 如果是场地管理员，必须检查管辖权
        if ("VENUE_ADMIN".equals(role)) {
            // 查询该场地是否归该管理员管
            VenueInfo venue = venueInfoService.getById(record.getVenueId());
            if (venue == null || !adminId.equals(venue.getAdminId())) {
                throw new RuntimeException("权限不足：您无权审批该场地的预约");
            }
        }

        // 2. 设置审批状态（使用你的状态码：1为驳回，2为通过）
        record.setStatus(status);
        record.setAuditadminid(adminId);
        record.setAuditTime(LocalDateTime.now());

        // 3. 处理驳回理由
        if (status == 1) { // 驳回状态
            record.setRejectReason(rejectReason);
        } else if (status == 2) { // 通过状态
            record.setRejectReason(""); // 审核通过时，清空之前的驳回理由
        }

        // 4. 保存并触发通知系统
        boolean success = updateById(record);
        if (success) {
            this.sendNotification(record);
        }
        return success;
    }

    @Override
    public Result getAdminRecordList(Integer tab, Long adminId, String role) {
        LambdaQueryWrapper<BookingRecord> recordWrapper = new LambdaQueryWrapper<>();

        // 只有场地管理员才需要过滤 VenueId
        if ("VENUE_ADMIN".equals(role)){
            List<Long> myVenueIds = venueInfoService.list(new LambdaQueryWrapper<VenueInfo>()
                            .eq(VenueInfo::getAdminId,adminId))
                    .stream()
                    .map(VenueInfo::getId)
                    .collect(Collectors.toList());

            if(myVenueIds.isEmpty()){
                return Result.success(new ArrayList<BookingRecord>());
            }

            recordWrapper.in(BookingRecord::getVenueId, myVenueIds);
        } else if (!"SYS_ADMIN".equals(role)) {
            // 如果既不是场地管理员也不是系统管理员，直接拦截（安全性兜底）
            return Result.error("权限不足，无法访问管理列表");
        }

        if (tab != null) {
            recordWrapper.eq(BookingRecord::getStatus, tab);
        }

        // 如果是 "SYS_ADMIN"，直接跳过上面的 if，不加 venueId 限制，即查询全校数据
        recordWrapper.orderByDesc(BookingRecord::getCreateTime);
        List<BookingRecord> records = this.list(recordWrapper);

        return Result.success(records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result blockVenueTime(Long venueId, BlockTimeDTO dto, Long adminId) {
        if (dto == null || dto.getBookingDate() == null || !StringUtils.hasText(dto.getTimeSlot())) {
            return Result.error("请输入要维护的日期与时间段");
        }

        VenueInfo venue = venueInfoService.getById(venueId);
        if (venue == null) {
            return Result.error("场地不存在");
        }

        String role = securityUtils.getUserRole();
        if ("VENUE_ADMIN".equals(role)) {
            if (!adminId.equals(venue.getAdminId())) {
                return Result.error("权限不足：您不是该场地管理员");
            }
        } else if (!"SYS_ADMIN".equals(role)) {
            return Result.error("权限不足");
        }

        String[] times = dto.getTimeSlot().split("-");
        if (times.length != 2) {
            return Result.error("时间段格式错误，应为 HH:mm-HH:mm");
        }
        String newStart = times[0].trim();
        String newEnd = times[1].trim();

        try {
            LocalTime.parse(newStart);
            LocalTime.parse(newEnd);
        } catch (DateTimeParseException ex) {
            return Result.error("时间段格式错误，应为 HH:mm-HH:mm");
        }

        LambdaQueryWrapper<BookingRecord> conflictWrapper = new LambdaQueryWrapper<>();
        conflictWrapper.eq(BookingRecord::getVenueId, venueId)
                .eq(BookingRecord::getBookingDate, dto.getBookingDate())
                .eq(BookingRecord::getStatus, 3)
                .and(w -> w.apply("SUBSTRING_INDEX(time_slot, '-', 1) < {0}", newEnd)
                        .apply("SUBSTRING_INDEX(time_slot, '-', -1) > {0}", newStart));

        if (bookingRecordMapper.selectCount(conflictWrapper) > 0) {
            return Result.error("该时段已存在维护记录");
        }

        BookingRecord record = new BookingRecord();
        record.setVenueId(venueId);
        record.setUserId(adminId);
        record.setEventName("场地维护");
        record.setHostUnit("场地管理");
        record.setExceptNum(0);
        record.setContactPerson("");
        record.setContactPhone("");
        record.setDescription(StringUtils.hasText(dto.getReason()) ? dto.getReason() : "场地维护/不可用");
        record.setBookingDate(dto.getBookingDate());
        record.setTimeSlot(dto.getTimeSlot().trim());
        record.setStatus(3);
        record.setAuditadminid(adminId);
        record.setAuditTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());

        SysUser adminUser = sysUserService.getById(adminId);
        if (adminUser != null) {
            if (!StringUtils.hasText(record.getContactPerson())) {
                record.setContactPerson(adminUser.getRealName());
            }
            if (!StringUtils.hasText(record.getContactPhone())) {
                record.setContactPhone(adminUser.getPhoneNumber());
            }
        }

        bookingRecordMapper.insert(record);

        LambdaQueryWrapper<BookingRecord> affectedWrapper = new LambdaQueryWrapper<>();
        affectedWrapper.eq(BookingRecord::getVenueId, venueId)
                .eq(BookingRecord::getBookingDate, dto.getBookingDate())
                .in(BookingRecord::getStatus, 0, 2)
                .and(w -> w.apply("SUBSTRING_INDEX(time_slot, '-', 1) < {0}", newEnd)
                        .apply("SUBSTRING_INDEX(time_slot, '-', -1) > {0}", newStart));

        List<BookingRecord> affected = bookingRecordMapper.selectList(affectedWrapper);
        if (!affected.isEmpty()) {
            for (BookingRecord booking : affected) {
                booking.setStatus(4);
                bookingRecordMapper.updateById(booking);

                Notification notice = new Notification();
                notice.setTargetUserId(booking.getUserId());
                notice.setSenderId(adminId);
                notice.setTitle("预约取消提醒");
                notice.setContent("抱歉，场地 [" + venue.getName() + "] 在 " + booking.getBookingDate() + " "
                        + booking.getTimeSlot() + " 需维护，您的预约已自动取消。");
                notice.setType(1);
                notice.setCreateTime(LocalDateTime.now());
                notificationService.save(notice);
            }
        }

        return Result.success("维护时段已更新");
    }

    //判断是否可取消预约
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

    private void sendNotification(BookingRecord record) {
        Notification notice = new Notification();

        String statusText = (record.getStatus() == 2) ? "通过" : "驳回";
        notice.setTitle("预约申请" + statusText + "通知");

        String content = String.format("您的活动 [%s] 预约申请已被管理员%s。",
                record.getEventName(), statusText);

        if(record.getStatus() == 1 && record.getRejectReason()!=null){
            content +="驳回原因："+record.getRejectReason();
        }
        notice.setContent(content);

        notice.setTargetUserId(record.getUserId());
        notice.setSenderId(record.getAuditadminid());

        notice.setType(1);
        notice.setCreateTime(LocalDateTime.now());

        notificationService.save(notice);

        System.out.println("通知已成功发送给用户ID: " + record.getUserId());

    }
}
