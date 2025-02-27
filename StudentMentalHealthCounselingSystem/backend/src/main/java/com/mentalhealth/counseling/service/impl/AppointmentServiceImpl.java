package com.mentalhealth.counseling.service.impl;

import com.mentalhealth.counseling.dto.AppointmentResponse;
import com.mentalhealth.counseling.dto.UserProfileResponse;
import com.mentalhealth.counseling.entity.Appointment;
import com.mentalhealth.counseling.entity.Appointment.AppointmentStatus;
import com.mentalhealth.counseling.entity.User;
import com.mentalhealth.counseling.exception.ResourceNotFoundException;
import com.mentalhealth.counseling.repository.AppointmentRepository;
import com.mentalhealth.counseling.repository.UserRepository;
import com.mentalhealth.counseling.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约服务实现类
 */
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getCounselorAppointments(
            Long counselorId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            List<AppointmentStatus> statuses,
            Pageable pageable
    ) {
        return appointmentRepository
                .findByCounselorIdAndAppointmentTimeBetweenAndStatusIn(
                        counselorId, startTime, endTime, statuses, pageable
                )
                .map(this::convertToAppointmentResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getStudentAppointments(
            Long studentId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            List<AppointmentStatus> statuses,
            Pageable pageable
    ) {
        return appointmentRepository
                .findByStudentIdAndAppointmentTimeBetweenAndStatusIn(
                        studentId, startTime, endTime, statuses, pageable
                )
                .map(this::convertToAppointmentResponse);
    }

    @Override
    @Transactional
    public AppointmentResponse createAppointment(
            Long studentId,
            Long counselorId,
            String subject,
            String description,
            LocalDateTime appointmentTime
    ) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("学生不存在"));
        User counselor = userRepository.findById(counselorId)
                .orElseThrow(() -> new ResourceNotFoundException("咨询师不存在"));

        Appointment appointment = Appointment.builder()
                .student(student)
                .counselor(counselor)
                .subject(subject)
                .description(description)
                .appointmentTime(appointmentTime)
                .status(AppointmentStatus.PENDING)
                .build();

        return convertToAppointmentResponse(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public void confirmAppointment(Long appointmentId, Long counselorId) {
        Appointment appointment = getAppointmentForCounselor(appointmentId, counselorId);
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new IllegalStateException("只能确认待确认状态的预约");
        }
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public void completeAppointment(Long appointmentId, Long counselorId) {
        Appointment appointment = getAppointmentForCounselor(appointmentId, counselorId);
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("只能完成已确认状态的预约");
        }
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public void cancelAppointment(Long appointmentId, Long userId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("预约不存在"));

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("已完成的预约不能取消");
        }

        if (!appointment.getStudent().getId().equals(userId) &&
            !appointment.getCounselor().getId().equals(userId)) {
            throw new IllegalStateException("只有预约相关的学生或咨询师可以取消预约");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    private Appointment getAppointmentForCounselor(Long appointmentId, Long counselorId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("预约不存在"));

        if (!appointment.getCounselor().getId().equals(counselorId)) {
            throw new IllegalStateException("只能操作自己的预约");
        }

        return appointment;
    }

    private AppointmentResponse convertToAppointmentResponse(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setSubject(appointment.getSubject());
        response.setDescription(appointment.getDescription());
        response.setAppointmentTime(appointment.getAppointmentTime());
        response.setStatus(appointment.getStatus());
        response.setCreateTime(appointment.getCreateTime());
        response.setUpdateTime(appointment.getUpdateTime());

        // 转换学生信息
        UserProfileResponse student = new UserProfileResponse();
        student.setId(appointment.getStudent().getId());
        student.setUsername(appointment.getStudent().getUsername());
        student.setRealName(appointment.getStudent().getRealName());
        student.setPhone(appointment.getStudent().getPhone());
        student.setEmail(appointment.getStudent().getEmail());
        student.setRole(appointment.getStudent().getRole());
        student.setUniversity(appointment.getStudent().getUniversity());
        student.setCollege(appointment.getStudent().getCollege());
        student.setMajor(appointment.getStudent().getMajor());
        student.setStudentId(appointment.getStudent().getStudentId());
        response.setStudent(student);

        // 转换咨询师信息
        UserProfileResponse counselor = new UserProfileResponse();
        counselor.setId(appointment.getCounselor().getId());
        counselor.setUsername(appointment.getCounselor().getUsername());
        counselor.setRealName(appointment.getCounselor().getRealName());
        counselor.setPhone(appointment.getCounselor().getPhone());
        counselor.setEmail(appointment.getCounselor().getEmail());
        counselor.setRole(appointment.getCounselor().getRole());
        response.setCounselor(counselor);

        return response;
    }
} 