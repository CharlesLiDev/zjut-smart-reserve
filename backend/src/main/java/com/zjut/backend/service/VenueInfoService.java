package com.zjut.backend.service;

import com.zjut.backend.dto.VenueVO;
import com.zjut.backend.entity.VenueInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author 15588
* @description 针对表【venue_info(场地信息表)】的数据库操作Service
* @createDate 2026-02-22 13:29:33
*/
public interface VenueInfoService extends IService<VenueInfo> {
    boolean assignVenueAdmin(Long venueId, Long adminId);

    List<VenueVO> getVenueListWithAdmin();

    @Transactional(rollbackFor = Exception.class)
    boolean updateStatusWithNotification(Long venueId, Integer newStatus);
}
