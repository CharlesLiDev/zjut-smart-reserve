package com.zjut.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjut.backend.entity.BookingRecord;
import com.zjut.backend.service.BookingRecordService;
import com.zjut.backend.mapper.BookingRecordMapper;
import org.springframework.stereotype.Service;

/**
* @author 15588
* @description 针对表【booking_record(预约申请表)】的数据库操作Service实现
* @createDate 2026-02-20 16:39:05
*/
@Service
public class BookingRecordServiceImpl extends ServiceImpl<BookingRecordMapper, BookingRecord>
    implements BookingRecordService{

}




