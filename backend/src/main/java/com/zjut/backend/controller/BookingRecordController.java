package com.zjut.backend.controller;


import com.zjut.backend.common.Result;
import com.zjut.backend.dto.AppointmentDTO;
import com.zjut.backend.service.BookingRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
public class BookingRecordController {
    @Autowired
    private BookingRecordService bookingRecordService;

    @PostMapping
    public Result postAppointment(@RequestBody AppointmentDTO dto){
        Long currentUserId=1L;
        return bookingRecordService.submitAppointment(dto,currentUserId);
    }
}
