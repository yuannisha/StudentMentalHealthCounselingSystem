package com.mentalhealth.counseling.service;

import com.mentalhealth.counseling.dto.UserProfileResponse;
import com.mentalhealth.counseling.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 管理员服务接口
 */
public interface AdminService {
    /**
     * 获取用户列表
     */
    Page<UserProfileResponse> getUsers(String search, UserRole role, Pageable pageable);

    /**
     * 删除用户
     */
    void deleteUser(Long userId);

    /**
     * 禁用/启用用户
     */
    void toggleUserStatus(Long userId, boolean enabled);
} 