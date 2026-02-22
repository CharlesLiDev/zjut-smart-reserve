package com.zjut.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjut.backend.common.Result;
import com.zjut.backend.dto.AppointmentDTO;
import com.zjut.backend.entity.BookingRecord;
import com.zjut.backend.service.BookingRecordService;
import com.zjut.backend.mapper.BookingRecordMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
* @author 15588
* @description 针对表【booking_record(预约申请表)】的数据库操作Service实现
* @createDate 2026-02-20 16:39:05
*/
@Service
public class BookingRecordServiceImpl extends ServiceImpl<BookingRecordMapper, BookingRecord>
    implements BookingRecordService{
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
        String[] times=dto.getTimeSlot().split("-");
        if (times.length != 2) {
            return Result.error("时间段格式错误，应为 HH:mm-HH:mm");
        }
        String newStart=times[0].trim();
        String newEnd=times[1].trim();

        //并发冲突检查
        LambdaQueryWrapper<BookingRecord> wrapper=new LambdaQueryWrapper<BookingRecord>();
        wrapper.eq(BookingRecord::getVenueId, dto.getVenueId())
                .eq(BookingRecord::getBookingDate, dto.getBookingDate())
                .in(BookingRecord::getStatus,0,2,3)
                .and(w -> w.apply("SUBSTRING_INDEX(time_slot, '-', 1) < {0}", newEnd)
                        .apply("SUBSTRING_INDEX(time_slot, '-', -1) > {0}", newStart));

        Long count=bookingRecordMapper.selectCount(wrapper);
        if(count>0){
            return Result.error("该时段已被占用或正在审核中，请刷新排期表重选");
        }

        BookingRecord record=new BookingRecord();
        BeanUtils.copyProperties(dto,record);
        record.setUserId(userId);
        record.setStatus(0);
        record.setCreateTime(LocalDateTime.now());

        bookingRecordMapper.insert(record);
        return Result.success("预约申请已提交，请等待管理员审核");
    }

    }




