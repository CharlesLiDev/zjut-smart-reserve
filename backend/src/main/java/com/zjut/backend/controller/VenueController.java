package com.zjut.backend.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjut.backend.common.Result;
import com.zjut.backend.dto.AppointmentDTO;
import com.zjut.backend.dto.VenueAssignDTO;
import com.zjut.backend.dto.VenueVO;
import com.zjut.backend.entity.BookingRecord;
import com.zjut.backend.entity.VenueInfo;
import com.zjut.backend.service.BookingRecordService;
import com.zjut.backend.service.VenueInfoService;
import com.zjut.backend.utils.SecurityUtils;
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
    @Autowired
    private SecurityUtils securityUtils;
    @Autowired
    private VenueInfoService venueInfoService;


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

    @GetMapping("/{id}")
    public Result getVenueById(@PathVariable Long id) {
        VenueInfo venueInfo = venueService.getById(id);
        if (venueInfo == null) {
            return Result.error("场地不存在");
        }
        venueInfo.setAvailabilityStatus(bookingRecordService.calculateAvailabilityStatus(id));
        return Result.success(venueInfo);
    }

    /**
     * 查询场地的预约记录
     * @param id 场地ID
     * @param date 查询日期
     * @return 该日期的预约记录列表
     */
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

    @PutMapping("/assign-admin")
    public Result assignAdmin(@RequestBody VenueAssignDTO dto) {
        String role= securityUtils.getUserRole();
        if(!"SYS_ADMIN".equals(role)){
            return Result.error("权限不足，仅系统管理员可操作");
        }

        boolean success=venueInfoService.assignVenueAdmin(dto.getVenueId(), dto.getAdminId());

        return success ? Result.success("分配成功") : Result.error("分配失败");
    }


    @GetMapping("/admin/list")
    public Result getAdminVenueList() {
        String role = securityUtils.getUserRole();
        if (!"SYS_ADMIN".equals(role)) {
            return Result.error("权限不足：只有系统管理员可以查看全局场地管理列表");
        }

        List<VenueVO> venueList = venueInfoService.getVenueListWithAdmin();

        return Result.success(venueList);
    }

    @GetMapping("/my/list")
    public Result getMyVenueList() {
        String role = securityUtils.getUserRole();
        Long userId = securityUtils.getCurrentUserId();

        if ("SYS_ADMIN".equals(role)) {
            return Result.success(venueInfoService.list());
        }
        if (!"VENUE_ADMIN".equals(role)) {
            return Result.error("权限不足");
        }

        List<VenueInfo> myVenues = venueInfoService.list(new LambdaQueryWrapper<VenueInfo>()
                .eq(VenueInfo::getAdminId, userId));
        return Result.success(myVenues);
    }

    @PutMapping("{id}/status")
    public Result updateVenueStatus(@PathVariable Long id,@RequestParam Integer status) {
        Long currentUserId = securityUtils.getCurrentUserId();
        String role = securityUtils.getUserRole();

        VenueInfo venue=venueInfoService.getById(id);
        if(venue==null){
            return Result.error("场地不存在");
        }
        if(!"SYS_ADMIN".equals(role)&&!currentUserId.equals(venue.getAdminId())){
            return Result.error("权限不足：您不是该场地的管理员");
        }
        boolean success=venueInfoService.updateStatusWithNotification(id, status);

        return success?Result.success("更新成功"):Result.error("更新失败");
    }

    @PutMapping("/update-info")
    public Result updateVenueInfo(@RequestBody VenueInfo venueInfo) {
        Long currentUserId = securityUtils.getCurrentUserId();
        String role = securityUtils.getUserRole();

        // 1. 获取数据库中该场地的原始信息
        VenueInfo oldVenue = venueInfoService.getById(venueInfo.getId());
        if (oldVenue == null) return Result.error("场地不存在");

        // 2. 权限校验逻辑
        if ("VENUE_ADMIN".equals(role)) {
            // 如果是场地管理员，检查该场地是否归他管
            if (!currentUserId.equals(oldVenue.getAdminId())) {
                return Result.error("权限不足：您不是该场地的管理员，无法修改信息");
            }
        } else if (!"SYS_ADMIN".equals(role)) {
            return Result.error("权限不足");
        }

        // 3. 执行更新 (限制只能更新特定字段，防止管理员把自己改掉)
        oldVenue.setDescription(venueInfo.getDescription());
        oldVenue.setImageUrl(venueInfo.getImageUrl());
        oldVenue.setEquipment(venueInfo.getEquipment());
        oldVenue.setCapacity(venueInfo.getCapacity());
        oldVenue.setLocation(venueInfo.getLocation());
        oldVenue.setType(venueInfo.getType());
        oldVenue.setName(venueInfo.getName());
        // ... 其他允许修改的字段

        venueInfoService.updateById(oldVenue);
        return Result.success("场地信息维护成功");
    }

    @GetMapping("/test")
    public String test() { return "ok"; }
}
