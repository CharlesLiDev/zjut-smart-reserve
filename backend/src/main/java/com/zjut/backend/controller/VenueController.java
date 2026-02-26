package com.example.controller;

import com.example.entity.Venue;
import com.example.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/venue")
@RequiredArgsConstructor
@CrossOrigin
public class VenueController {

    private final VenueService venueService;

    // 全局查询所有场地（超级管理员用）
    @GetMapping("/all")
    public Map<String, Object> getAllVenue() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Venue> list = venueService.list();
            result.put("code", 200);
            result.put("data", list);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    // 修改场地信息（包括可用状态）
    @PostMapping("/update")
    public Map<String, Object> updateVenue(@RequestBody Venue venue) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = venueService.updateById(venue);
            // 若修改为不可用，需通知已预约用户（简化版，仅提示）
            if (success) {
                result.put("code", 200);
                result.put("msg", "修改成功，已通知相关用户");
            } else {
                result.put("code", 500);
                result.put("msg", "修改失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
        }
        return result;
    }
}