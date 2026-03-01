package com.zjut.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class AppointmentDTO {
    private Long venueId;
    private String eventName;
    private String hostUnit;
    private Integer exceptNum;
    private String contactPerson;
    private String contactPhone;
    private String description;
    private String planDocUrl;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate bookingDate;
    private String timeSlot;
}
