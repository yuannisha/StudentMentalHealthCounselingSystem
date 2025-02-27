package com.mentalhealth.counseling.controller;

import com.mentalhealth.counseling.dto.UserProfileResponse;
import com.mentalhealth.counseling.entity.UserRole;
import com.mentalhealth.counseling.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 获取用户列表
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserProfileResponse>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserRole role,
            Pageable pageable
    ) {
        return ResponseEntity.ok(adminService.getUsers(search, role, pageable));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 禁用/启用用户
     */
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<Void> toggleUserStatus(
            @PathVariable Long userId,
            @RequestParam boolean enabled
    ) {
        adminService.toggleUserStatus(userId, enabled);
        return ResponseEntity.ok().build();
    }
} 