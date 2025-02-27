package com.mentalhealth.counseling.repository;

import com.mentalhealth.counseling.entity.Appointment;
import com.mentalhealth.counseling.entity.Appointment.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约数据访问接口
 */
public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {
    /**
     * 查找咨询师的预约列表
     */
    Page<Appointment> findByCounselorIdAndAppointmentTimeBetweenAndStatusIn(
            Long counselorId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            List<AppointmentStatus> statuses,
            Pageable pageable
    );

    /**
     * 查找学生的预约列表
     */
    Page<Appointment> findByStudentIdAndAppointmentTimeBetweenAndStatusIn(
            Long studentId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            List<AppointmentStatus> statuses,
            Pageable pageable
    );
} 