package com.mentalhealth.counseling.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建预约请求DTO
 */
@Data
public class CreateAppointmentRequest {
    @NotNull(message = "咨询师ID不能为空")
    private Long counselorId;

    @NotBlank(message = "咨询主题不能为空")
    private String subject;

    private String description;

    @NotNull(message = "预约时间不能为空")
    @Future(message = "预约时间必须是将来的时间")
    private LocalDateTime appointmentTime;
} 