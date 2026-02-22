package com.zjut.backend.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjut.backend.common.Result;
import com.zjut.backend.entity.VenueInfo;
import com.zjut.backend.service.VenueInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/venue")
public class VenueController {

    @Autowired
    private VenueInfoService venueService;

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
        return Result.success(result);
    }


}
