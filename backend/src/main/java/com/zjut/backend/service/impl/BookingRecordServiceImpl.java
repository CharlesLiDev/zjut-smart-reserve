package com.zjut.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjut.backend.common.Result;
import com.zjut.backend.dto.AppointmentDTO;
import com.zjut.backend.dto.AppointmentVO;
import com.zjut.backend.dto.MyAppointmentQueryDTO;
import com.zjut.backend.entity.BookingRecord;
import com.zjut.backend.service.BookingRecordService;
import com.zjut.backend.mapper.BookingRecordMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author 15588
 * @description 针对表【booking_record(预约申请表)】的数据库操作Service实现
 * @createDate 2026-02-20 16:39:05
 */
@Service
public class BookingRecordServiceImpl extends ServiceImpl<BookingRecordMapper, BookingRecord>
        implements BookingRecordService {
    private final BookingRecordMapper bookingRecordMapper;

    public BookingRecordServiceImpl(BookingRecordMapper bookingRecordMapper) {
        this.bookingRecordMapper = bookingRecordMapper;
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
        record.setCreateTime(LocalDateTime.now());

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
}




