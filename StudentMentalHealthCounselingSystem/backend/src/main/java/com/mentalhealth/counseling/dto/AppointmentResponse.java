package com.mentalhealth.counseling.dto;

import com.mentalhealth.counseling.entity.Appointment.AppointmentStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预约响应DTO
 */
@Data
public class AppointmentResponse {
    private Long id;
    private UserProfileResponse student;
    private UserProfileResponse counselor;
    private String subject;
    private String description;
    private LocalDateTime appointmentTime;
    private AppointmentStatus status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 