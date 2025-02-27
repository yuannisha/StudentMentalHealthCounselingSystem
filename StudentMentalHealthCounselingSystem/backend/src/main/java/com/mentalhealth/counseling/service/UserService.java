package com.mentalhealth.counseling.service;

import com.mentalhealth.counseling.dto.ChangePasswordRequest;
import com.mentalhealth.counseling.dto.UserProfileRequest;
import com.mentalhealth.counseling.dto.UserProfileResponse;
import com.mentalhealth.counseling.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 用户服务接口
 */
public interface UserService {
    /**
     * 获取用户信息
     */
    UserProfileResponse getUserProfile(String username);

    /**
     * 更新用户信息
     */
    UserProfileResponse updateUserProfile(String username, UserProfileRequest request);

    /**
     * 修改密码
     */
    void changePassword(String username, ChangePasswordRequest request);
    
    /**
     * 按角色获取用户列表
     *
     * @param search 搜索关键词（用户名、真实姓名、邮箱或电话）
     * @param role 用户角色
     * @param pageable 分页参数
     * @return 用户列表
     */
    Page<UserProfileResponse> getUsersByRole(String search, UserRole role, Pageable pageable);
    
    /**
     * 获取学生列表（供咨询师使用）
     *
     * @param page 页码
     * @param size 每页记录数
     * @param keyword 搜索关键词（用户名、真实姓名、学号等）
     * @return 学生列表（分页）
     */
    Page<UserProfileResponse> getStudents(int page, int size, String keyword);
} 