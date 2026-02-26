package com.zjut.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjut.backend.dto.VenueVO;
import com.zjut.backend.entity.BookingRecord;
import com.zjut.backend.entity.Notification;
import com.zjut.backend.entity.SysUser;
import com.zjut.backend.entity.VenueInfo;
import com.zjut.backend.service.BookingRecordService;
import com.zjut.backend.service.NotificationService;
import com.zjut.backend.service.SysUserService;
import com.zjut.backend.service.VenueInfoService;
import com.zjut.backend.mapper.VenueInfoMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 15588
 * @description 针对表【venue_info(场地信息表)】的数据库操作Service实现
 * @createDate 2026-02-22 13:29:33
 */
@Service
public class VenueInfoServiceImpl extends ServiceImpl<VenueInfoMapper, VenueInfo>
        implements VenueInfoService {

    @Autowired
    @Lazy
    private BookingRecordService bookingRecordService;

    @Autowired
    private final NotificationService notificationService;

    @Autowired
    private final SysUserService sysUserService;

    public VenueInfoServiceImpl(NotificationService notificationService, SysUserService sysUserService) {
        this.notificationService = notificationService;
        this.sysUserService = sysUserService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignVenueAdmin(Long venueId, Long adminId) {
        VenueInfo venue = this.getById(venueId);
        if (venue == null) {
            throw new RuntimeException("场地不存在");
        }
        venue.setAdminId(adminId);
        boolean updated = this.updateById(venue);
        if (updated) {
            Notification notification = new Notification();
            notification.setTargetUserId(adminId);
            notification.setTitle("管辖权分配通知");
            notification.setContent("您已被设置为场地 [" + venue.getName() + "] 的管理员，请及时处理相关预约申请。");
            notification.setType(1); // 1-私信/任务通知
            notification.setCreateTime(LocalDateTime.now());
            notificationService.save(notification);
        }

        return updated;
    }

    @Override
    public List<VenueVO> getVenueListWithAdmin() {
        // 1. 查出所有场地
        List<VenueInfo> venues = this.list();

        // 2. 查出所有场地管理员（减少数据库访问次数，一次性查出）
        List<SysUser> admins = sysUserService.list(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRole, "VENUE_ADMIN"));

        // 3. 将管理员列表转为 Map (ID -> Name) 方便匹配
        Map<Long, String> adminMap = admins.stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getRealName));

        // 4. 封装成 VO 返回给前端
        return venues.stream().map(v -> {
            VenueVO vo = new VenueVO();
            BeanUtils.copyProperties(v, vo);
            vo.setAdminName(adminMap.getOrDefault(v.getAdminId(), "未分配"));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatusWithNotification(Long venueId, Integer newStatus) {
        VenueInfo venue = this.getById(venueId);
        venue.setStatus(newStatus);
        this.updateById(venue);

        // 2. 如果状态变为“维护”或“禁用”，处理已预约的订单
        if (newStatus == 1 || newStatus == 2) {
            String statusReason = (newStatus == 1) ? "设备维护" : "场地关闭";

            // 查询该场地未来所有有效的预约 (状态为待审核0 或 已通过2)
            List<BookingRecord> affectedRecords = bookingRecordService.list(new LambdaQueryWrapper<BookingRecord>()
                    .eq(BookingRecord::getVenueId, venueId)
                    .in(BookingRecord::getStatus, 0, 2)
                    .ge(BookingRecord::getBookingDate, LocalDate.now())); // 仅限今天及以后

            if (!affectedRecords.isEmpty()) {
                for (BookingRecord record : affectedRecords) {
                    // 更新订单状态为“已取消(由于场地原因)”
                    record.setStatus(4);
                    bookingRecordService.updateById(record);

                    // 发送通知给学生
                    Notification notice = new Notification();
                    notice.setTargetUserId(record.getUserId());
                    notice.setTitle("预约取消提醒");
                    notice.setContent("抱歉，由于场地 [" + venue.getName() + "] 正在进行" + statusReason +
                            "，您在 " + record.getBookingDate() + " " + record.getTimeSlot() +
                            " 的预约已被系统自动取消。");
                    notice.setType(1); // 私信通知
                    notice.setCreateTime(LocalDateTime.now());
                    notificationService.save(notice);
                }
            }
        }
        return true;
    }
}




