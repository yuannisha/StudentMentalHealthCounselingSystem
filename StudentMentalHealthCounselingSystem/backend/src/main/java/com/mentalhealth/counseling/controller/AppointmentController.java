package com.mentalhealth.counseling.controller;

import com.mentalhealth.counseling.dto.AppointmentResponse;
import com.mentalhealth.counseling.dto.CreateAppointmentRequest;
import com.mentalhealth.counseling.entity.Appointment.AppointmentStatus;
import com.mentalhealth.counseling.entity.User;
import com.mentalhealth.counseling.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约控制器
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * 获取咨询师的预约列表
     */
    @GetMapping("/counselor")
    @PreAuthorize("hasRole('COUNSELOR')")
    public ResponseEntity<Page<AppointmentResponse>> getCounselorAppointments(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) List<AppointmentStatus> statuses,
            @RequestParam(required = false) AppointmentStatus status,
            Pageable pageable
    ) {
        List<AppointmentStatus> statusList = null;
        
        // 优先使用statuses参数
        if (statuses != null && !statuses.isEmpty()) {
            statusList = statuses;
        } 
        // 如果提供了单个status参数
        else if (status != null) {
            statusList = List.of(status);
        }
        // 如果都没有提供，使用所有状态
        else {
            statusList = List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED, 
                               AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED);
        }
        
        return ResponseEntity.ok(appointmentService.getCounselorAppointments(
                user.getId(),
                startTime != null ? startTime : LocalDateTime.now().minusMonths(1),
                endTime != null ? endTime : LocalDateTime.now().plusMonths(1),
                statusList,
                pageable
        ));
    }

    /**
     * 获取学生的预约列表
     */
    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Page<AppointmentResponse>> getStudentAppointments(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) List<AppointmentStatus> statuses,
            @RequestParam(required = false) AppointmentStatus status,
            Pageable pageable
    ) {
        List<AppointmentStatus> statusList = null;
        
        // 优先使用statuses参数
        if (statuses != null && !statuses.isEmpty()) {
            statusList = statuses;
        } 
        // 如果提供了单个status参数
        else if (status != null) {
            statusList = List.of(status);
        }
        // 如果都没有提供，使用所有状态
        else {
            statusList = List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED, 
                               AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED);
        }
        
        return ResponseEntity.ok(appointmentService.getStudentAppointments(
                user.getId(),
                startTime != null ? startTime : LocalDateTime.now().minusMonths(1),
                endTime != null ? endTime : LocalDateTime.now().plusMonths(1),
                statusList,
                pageable
        ));
    }

    /**
     * 创建预约
     */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AppointmentResponse> createAppointment(
            @AuthenticationPrincipal User user,
            @RequestBody CreateAppointmentRequest request
    ) {
        return ResponseEntity.ok(appointmentService.createAppointment(
                user.getId(),
                request.getCounselorId(),
                request.getSubject(),
                request.getDescription(),
                request.getAppointmentTime()
        ));
    }

    /**
     * 确认预约
     */
    @PutMapping("/{appointmentId}/confirm")
    @PreAuthorize("hasRole('COUNSELOR')")
    public ResponseEntity<Void> confirmAppointment(
            @AuthenticationPrincipal User user,
            @PathVariable Long appointmentId
    ) {
        appointmentService.confirmAppointment(appointmentId, user.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * 完成预约
     */
    @PutMapping("/{appointmentId}/complete")
    @PreAuthorize("hasRole('COUNSELOR')")
    public ResponseEntity<Void> completeAppointment(
            @AuthenticationPrincipal User user,
            @PathVariable Long appointmentId
    ) {
        appointmentService.completeAppointment(appointmentId, user.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * 取消预约
     */
    @PutMapping("/{appointmentId}/cancel")
    public ResponseEntity<Void> cancelAppointment(
            @AuthenticationPrincipal User user,
            @PathVariable Long appointmentId
    ) {
        appointmentService.cancelAppointment(appointmentId, user.getId());
        return ResponseEntity.ok().build();
    }
} 