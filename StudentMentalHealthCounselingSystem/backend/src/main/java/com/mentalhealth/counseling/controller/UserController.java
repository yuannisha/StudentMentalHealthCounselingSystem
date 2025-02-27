package com.mentalhealth.counseling.controller;

import com.mentalhealth.counseling.dto.ChangePasswordRequest;
import com.mentalhealth.counseling.dto.UserProfileRequest;
import com.mentalhealth.counseling.dto.UserProfileResponse;
import com.mentalhealth.counseling.entity.UserRole;
import com.mentalhealth.counseling.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(userService.getUserProfile(userDetails.getUsername()));
    }

    /**
     * 更新当前用户信息
     */
    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateCurrentUserProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid UserProfileRequest request
    ) {
        return ResponseEntity.ok(userService.updateUserProfile(userDetails.getUsername(), request));
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid ChangePasswordRequest request
    ) {
        userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取咨询师列表
     */
    @GetMapping("/counselors")
    public ResponseEntity<Page<UserProfileResponse>> getCounselors(
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        return ResponseEntity.ok(userService.getUsersByRole(search, UserRole.COUNSELOR, pageable));
    }
} 