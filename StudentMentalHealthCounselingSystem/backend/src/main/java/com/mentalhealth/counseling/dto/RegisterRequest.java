package com.mentalhealth.counseling.dto;

import com.mentalhealth.counseling.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册请求DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    @NotBlank(message = "真实姓名不能为空")
    private String realName;
    
    @NotBlank(message = "手机号不能为空")
    private String phone;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @NotNull(message = "用户角色不能为空")
    private UserRole role;
    
    private String university;
    private String college;
    private String major;
    private String studentId;
} 