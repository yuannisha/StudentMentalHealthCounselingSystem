package com.mentalhealth.counseling.dto;

import com.mentalhealth.counseling.entity.UserRole;
import lombok.Data;

/**
 * 用户信息响应DTO
 */
@Data
public class UserProfileResponse {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private UserRole role;
    private boolean enabled;
    private String university;
    private String college;
    private String major;
    private String studentId;
    private String createdAt;
    private String updatedAt;
} 