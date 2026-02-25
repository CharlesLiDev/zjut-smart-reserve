package com.zjut.backend.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjut.backend.common.Result;
import com.zjut.backend.dto.AppointmentDTO;
import com.zjut.backend.entity.BookingRecord;
import com.zjut.backend.entity.VenueInfo;
import com.zjut.backend.service.BookingRecordService;
import com.zjut.backend.service.VenueInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/venue")
public class VenueController {

    @Autowired
    private VenueInfoService venueService;
    @Autowired
    private BookingRecordService bookingRecordService;

    @GetMapping
    public Result getVenues(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String equipment,
            @RequestParam(required = false) Integer capacityMin,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<VenueInfo> pageConfig = new Page<>(page, size);
        LambdaQueryWrapper<VenueInfo> wrapper = new LambdaQueryWrapper<>();

        // 只查询可用的场地
        wrapper.eq(VenueInfo::getStatus, 0);

        // 关键词模糊搜索
        if (StringUtils.hasText(name)) {
            wrapper.and(w -> w.like(VenueInfo::getName, name)
                    .or()
                    .like(VenueInfo::getLocation, name));
        }

        // 类型筛选
        wrapper.eq(StringUtils.hasText(type), VenueInfo::getType, type);

        // 容量筛选(大于等于)
        wrapper.ge(capacityMin != null, VenueInfo::getCapacity, capacityMin);

        // 设备筛选(针对SET类型的模糊查询)
        wrapper.like(equipment != null, VenueInfo::getEquipment, equipment);

        IPage<VenueInfo> result= venueService.page(pageConfig,wrapper);
        List<VenueInfo> records = result.getRecords();

        for(VenueInfo venue : records){
            Integer avaliabilityStatus = bookingRecordService.calculateAvailabilityStatus(venue.getId());
            venue.setAvailabilityStatus(avaliabilityStatus);
        }

        return Result.success(result);
    }

    @GetMapping("{id}/schedule")
    public Result getVenueSchedule(@PathVariable Long id,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        //构建查询条件
        LambdaQueryWrapper<BookingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookingRecord::getVenueId,id)
                .eq(BookingRecord::getBookingDate,date)
                //0:待审核, 2:已通过, 3:已使用
                .in(BookingRecord::getStatus,0,2,3);

        //执行查询
        List<BookingRecord> occupiedList= bookingRecordService.list(wrapper);

        return Result.success(occupiedList);
    }

    @GetMapping("/test")
    public String test() { return "ok"; }
}

