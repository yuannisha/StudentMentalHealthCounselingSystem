package com.mentalhealth.counseling.service;

import com.mentalhealth.counseling.dto.AppointmentResponse;
import com.mentalhealth.counseling.entity.Appointment.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约服务接口
 */
public interface AppointmentService {
    /**
     * 获取咨询师的预约列表
     */
    Page<AppointmentResponse> getCounselorAppointments(
            Long counselorId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            List<AppointmentStatus> statuses,
            Pageable pageable
    );

    /**
     * 获取学生的预约列表
     */
    Page<AppointmentResponse> getStudentAppointments(
            Long studentId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            List<AppointmentStatus> statuses,
            Pageable pageable
    );

    /**
     * 创建预约
     */
    AppointmentResponse createAppointment(Long studentId, Long counselorId, String subject, String description, LocalDateTime appointmentTime);

    /**
     * 确认预约
     */
    void confirmAppointment(Long appointmentId, Long counselorId);

    /**
     * 完成预约
     */
    void completeAppointment(Long appointmentId, Long counselorId);

    /**
     * 取消预约
     */
    void cancelAppointment(Long appointmentId, Long userId);
} 