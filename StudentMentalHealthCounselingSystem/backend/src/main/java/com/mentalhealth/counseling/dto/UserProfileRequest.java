package com.mentalhealth.counseling.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户信息更新请求DTO
 */
@Data
public class UserProfileRequest {
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String university;
    private String college;
    private String major;
    private String studentId;
} 